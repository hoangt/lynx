import os
import time
import sys
import shutil

#import qsys_utils
#from qsys_utils import *

def print_preamble(top_file, project_name, num_master, multiclock):
    #clock and reset
    print >>top_file, "// m2_s_w32_p4_c0.v\n\
// Generated using ACDS version 14.0 200 at 2015.06.13.17:30:41\n\
// Hacked by Mohamed Abdelfattah at "+time.strftime("%c")+"\n\n\
`timescale 1 ns / 1 ps\n\
module "+project_name+" ();\n\n\
    reg clk_clk;\n\
    reg reset_reset_n;\n\
    \
    //clocking\n\
    initial clk_clk = 1'b1;\n\
    always #5 clk_clk = ~clk_clk;"

    if multiclock==1:
        print >>top_file, "\treg clk_0_clk;\n\
    reg reset_0_reset_n;\n\
    initial clk_0_clk = 1'b1;\n\
    always #5 clk_0_clk = ~clk_0_clk;\n\
    initial begin\n\
        reset_0_reset_n = 1'b0;\n\
        #25;\n\
        reset_0_reset_n = 1'b1;\n\
    end\n"
    
    print >>top_file, "\t//reset\n\
    initial begin\n\
        reset_reset_n = 1'b0;\n\
        #25;\n\
        reset_reset_n = 1'b1;\n\
    end\n"
    
    
    #done wire definitions
    for i in range(num_master):
        print >>top_file, "\twire qsys_master_"+str(i)+"_done_done;"
    print >>top_file, "\twire qsys_slave_0_done_done;"
    print >>top_file, "\twire qsys_slave_0_readdatavalid_readdatavalid;\n"
    
    #halt sim module
    print >>top_file, "\t// Halt the simulation when done\n\
    //synopsys translate off\n\
    always @ (*)\n\
    if(",
    
    for i in range(num_master):
        if i == num_master-1:
            print >>top_file, "qsys_master_"+str(i)+"_done_done",
        else:
            print >>top_file, "qsys_master_"+str(i)+"_done_done | ",
            
            
    print >>top_file,")\n\
    begin\n\
        $display(\"Shutting down simulation, good bye, thank you for playing with the lynx simulator (Qsys edition)!\");\n\
        $finish(0);\n\
    end\n\
    //synopsys translate on\n"
    

#********************************************************************************************#
#                     PROGRAM START
#********************************************************************************************#

def generate_bus(num_master,width,num_pipeline,multiclock):

    project_name = 'm'+str(num_master)+'_w'+str(width)+'_p'+str(num_pipeline)+"_c"+str(multiclock);
    project_path = "../experiments/"+project_name

    if os.path.exists('../experiments/'+project_name+'/simulation/mentor/reports/qsys_trace.txt'):
        print "Simulation already run for "+project_name+", skipping this run.."
    else:

        #--------------------------------------------------------------------------------------------#
        # Generate Qsys file then generate simulation outputs
        #--------------------------------------------------------------------------------------------#

        #create project directory and delete any old one
        if os.path.exists(project_path):
            shutil.rmtree(project_path)
        os.mkdir(project_path)
            
        #copy base project from origs to experiments and rename it
        if multiclock == 0:
            shutil.copy("../origs/m"+str(num_master)+".qsys", project_path+"/"+project_name+".qsys")
        else:
            shutil.copy("../origs/m"+str(num_master)+"_clk.qsys", project_path+"/"+project_name+".qsys")
            
        shutil.copy("../origs/qsys_master_hw.tcl",project_path)
        shutil.copy("../origs/qsys_slave_hw.tcl",project_path)
        
        #change directory
        os.chdir(project_path)
        
        #
        #make any changes to the qsys file here
        #
        
        #generate testbench files
        #os.system("qsys-generate --testbench=STANDARD --testbench-simulation=VERILOG "+project_name+".qsys")
        os.system("ip-generate --search-path=\".,$\" --project-directory=. --output-directory=simulation --file-set=SIM_VERILOG --report-file=spd:"+project_name+".spd --system-info=DEVICE_FAMILY=\"Stratix V\" --system-info=DEVICE=5SGSED8K1F40C2 --system-info=DEVICE_SPEEDGRADE=2_H1 --component-file="+project_name+".qsys")
        os.system("ip-make-simscript --spd="+project_name+".spd --output-directory=simulation")

        #--------------------------------------------------------------------------------------------#
        # Hack top level and add clock, reset, and halt_sim
        #--------------------------------------------------------------------------------------------#
        
        #dive into sim directory
        os.chdir("simulation")
        
        #make a temp copy of top
        shutil.copy(project_name+".v","temp.v")
        
        #open top-level file
        top_orig_file = open("temp.v")
        top_file = open(project_name+".v",'w')
        
        #hack my own preamble in
        print_preamble(top_file,project_name,num_master,multiclock)
        
        #print rest of qsys file
        copy_flag=False
        for line in top_orig_file:
            if copy_flag:
                print >>top_file, line,
            if ");" in line:
                print >>top_file, "\t// Qsys-file continue"
                copy_flag = True
                
        
        top_orig_file.close()
        top_file.close()
        
        #remove that temp file now
        os.remove("temp.v")
        
        #--------------------------------------------------------------------------------------------#
        # run simulation
        #--------------------------------------------------------------------------------------------#
        
        #dive into msim directory
        os.chdir("mentor")
        os.mkdir("reports")
        
        os.system("vsim -c -do \"source msim_setup.tcl; ld; run -all;\"")
        
        #change directory back
        os.chdir("../../../../scripts")

    #--------------------------------------------------------------------------------------------#
    # copy report to scripts directory
    #--------------------------------------------------------------------------------------------#

    shutil.copy(project_path+"/simulation/mentor/reports/qsys_trace.txt", "all_reports/"+project_name+".txt")
     
    #--------------------------------------------------------------------------------------------#
    # Done
    #--------------------------------------------------------------------------------------------#