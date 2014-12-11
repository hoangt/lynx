package lynx.data;

import lynx.data.MyEnums.TranslatorType;

/**
 * Translators are modules between a DesignModule bundle and an Noc
 * 
 * @author Mohamed
 *
 */
public abstract class Translator extends Module {

    protected Noc parentNoc;
    protected Module parentModule;
    protected Bundle parentBundle;

    protected TranslatorType type;

    public Translator(Noc parentNoc, Module parentModule, Bundle parentBundle, TranslatorType type) {
        super(type.toString(), parentModule.getName() + "_" + type.toShortString());
        this.parentNoc = parentNoc;
        this.parentModule = parentModule;
        this.parentBundle = parentBundle;
        parentBundle.setTranslator(this);
        this.type = type;
    }

    public Module getParentModule() {
        return parentModule;
    }

    public Bundle getParentBundle() {
        return parentBundle;
    }

    public TranslatorType TranslatorType() {
        return type;
    }

}