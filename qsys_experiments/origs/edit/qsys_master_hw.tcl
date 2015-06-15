# TCL File Generated by Component Editor 14.0
# Sat Jun 13 14:11:17 EDT 2015
# DO NOT MODIFY


# 
# qsys_master "qsys_master" v1.0
# Mohamed 2015.06.13.14:11:17
# 
# 

# 
# request TCL package from ACDS 14.0
# 
package require -exact qsys 14.0


# 
# module qsys_master
# 
set_module_property DESCRIPTION ""
set_module_property NAME qsys_master
set_module_property VERSION 1.0
set_module_property INTERNAL false
set_module_property OPAQUE_ADDRESS_MAP true
set_module_property GROUP custom
set_module_property AUTHOR Mohamed
set_module_property DISPLAY_NAME qsys_master
set_module_property INSTANTIATE_IN_SYSTEM_MODULE true
set_module_property EDITABLE true
set_module_property REPORT_TO_TALKBACK false
set_module_property ALLOW_GREYBOX_GENERATION false
set_module_property REPORT_HIERARCHY false


# 
# file sets
# 
add_fileset QUARTUS_SYNTH QUARTUS_SYNTH "" ""
set_fileset_property QUARTUS_SYNTH TOP_LEVEL qsys_master
set_fileset_property QUARTUS_SYNTH ENABLE_RELATIVE_INCLUDE_PATHS false
set_fileset_property QUARTUS_SYNTH ENABLE_FILE_OVERWRITE_MODE false
add_fileset_file qsys_master.sv SYSTEM_VERILOG PATH ../../../verilog_common/qsys/qsys_master.sv TOP_LEVEL_FILE

add_fileset SIM_VERILOG SIM_VERILOG "" ""
set_fileset_property SIM_VERILOG TOP_LEVEL qsys_master
set_fileset_property SIM_VERILOG ENABLE_RELATIVE_INCLUDE_PATHS false
set_fileset_property SIM_VERILOG ENABLE_FILE_OVERWRITE_MODE false
add_fileset_file qsys_master.sv SYSTEM_VERILOG PATH ../../../verilog_common/qsys/qsys_master.sv


# 
# parameters
# 
add_parameter WIDTH INTEGER 32
set_parameter_property WIDTH DEFAULT_VALUE 32
set_parameter_property WIDTH DISPLAY_NAME WIDTH
set_parameter_property WIDTH TYPE INTEGER
set_parameter_property WIDTH UNITS None
set_parameter_property WIDTH ALLOWED_RANGES -2147483648:2147483647
set_parameter_property WIDTH HDL_PARAMETER true
add_parameter SRC_ID INTEGER 2 ""
set_parameter_property SRC_ID DEFAULT_VALUE 2
set_parameter_property SRC_ID DISPLAY_NAME SRC_ID
set_parameter_property SRC_ID WIDTH ""
set_parameter_property SRC_ID TYPE INTEGER
set_parameter_property SRC_ID UNITS None
set_parameter_property SRC_ID ALLOWED_RANGES -2147483648:2147483647
set_parameter_property SRC_ID DESCRIPTION ""
set_parameter_property SRC_ID HDL_PARAMETER true
add_parameter SNK_ID INTEGER 3 ""
set_parameter_property SNK_ID DEFAULT_VALUE 3
set_parameter_property SNK_ID DISPLAY_NAME SNK_ID
set_parameter_property SNK_ID WIDTH ""
set_parameter_property SNK_ID TYPE INTEGER
set_parameter_property SNK_ID UNITS None
set_parameter_property SNK_ID ALLOWED_RANGES -2147483648:2147483647
set_parameter_property SNK_ID DESCRIPTION ""
set_parameter_property SNK_ID HDL_PARAMETER true
add_parameter DST_ID INTEGER 0 ""
set_parameter_property DST_ID DEFAULT_VALUE 0
set_parameter_property DST_ID DISPLAY_NAME DST_ID
set_parameter_property DST_ID WIDTH ""
set_parameter_property DST_ID TYPE INTEGER
set_parameter_property DST_ID UNITS None
set_parameter_property DST_ID ALLOWED_RANGES -2147483648:2147483647
set_parameter_property DST_ID DESCRIPTION ""
set_parameter_property DST_ID HDL_PARAMETER true
add_parameter ADDR_WIDTH INTEGER 32
set_parameter_property ADDR_WIDTH DEFAULT_VALUE 32
set_parameter_property ADDR_WIDTH DISPLAY_NAME ADDR_WIDTH
set_parameter_property ADDR_WIDTH TYPE INTEGER
set_parameter_property ADDR_WIDTH UNITS None
set_parameter_property ADDR_WIDTH ALLOWED_RANGES -2147483648:2147483647
set_parameter_property ADDR_WIDTH HDL_PARAMETER true


# 
# display items
# 


# 
# connection point done
# 
add_interface done conduit end
set_interface_property done associatedClock clk
set_interface_property done associatedReset rst
set_interface_property done ENABLED true
set_interface_property done EXPORT_OF ""
set_interface_property done PORT_NAME_MAP ""
set_interface_property done CMSIS_SVD_VARIABLES ""
set_interface_property done SVD_ADDRESS_GROUP ""

add_interface_port done done done Output 1


# 
# connection point avl
# 
add_interface avl avalon start
set_interface_property avl addressUnits SYMBOLS
set_interface_property avl associatedClock clk
set_interface_property avl associatedReset rst
set_interface_property avl bitsPerSymbol 8
set_interface_property avl burstOnBurstBoundariesOnly false
set_interface_property avl burstcountUnits WORDS
set_interface_property avl doStreamReads false
set_interface_property avl doStreamWrites false
set_interface_property avl holdTime 0
set_interface_property avl linewrapBursts false
set_interface_property avl maximumPendingReadTransactions 0
set_interface_property avl maximumPendingWriteTransactions 0
set_interface_property avl readLatency 0
set_interface_property avl readWaitTime 0
set_interface_property avl setupTime 0
set_interface_property avl timingUnits Cycles
set_interface_property avl writeWaitTime 0
set_interface_property avl ENABLED true
set_interface_property avl EXPORT_OF ""
set_interface_property avl PORT_NAME_MAP ""
set_interface_property avl CMSIS_SVD_VARIABLES ""
set_interface_property avl SVD_ADDRESS_GROUP ""

add_interface_port avl address address Output ADDR_WIDTH
add_interface_port avl write write Output 1
add_interface_port avl read read Output 1
add_interface_port avl readdata readdata Input WIDTH
add_interface_port avl readdatavalid readdatavalid Input 1
add_interface_port avl waitrequest waitrequest Input 1
add_interface_port avl writedata writedata Output WIDTH


# 
# connection point rst
# 
add_interface rst reset end
set_interface_property rst associatedClock clk
set_interface_property rst synchronousEdges DEASSERT
set_interface_property rst ENABLED true
set_interface_property rst EXPORT_OF ""
set_interface_property rst PORT_NAME_MAP ""
set_interface_property rst CMSIS_SVD_VARIABLES ""
set_interface_property rst SVD_ADDRESS_GROUP ""

add_interface_port rst rst reset Input 1


# 
# connection point clk
# 
add_interface clk clock end
set_interface_property clk clockRate 0
set_interface_property clk ENABLED true
set_interface_property clk EXPORT_OF ""
set_interface_property clk PORT_NAME_MAP ""
set_interface_property clk CMSIS_SVD_VARIABLES ""
set_interface_property clk SVD_ADDRESS_GROUP ""

add_interface_port clk clk clk Input 1

