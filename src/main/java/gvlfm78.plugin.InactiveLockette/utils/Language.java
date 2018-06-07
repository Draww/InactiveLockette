package gvlfm78.plugin.InactiveLockette.utils;

import java.util.Locale;

public enum Language {

    English ("enGB"),
    Italian ("itIT"),
    French ("frFR");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String getCode(){
        return code;
    }

    public String getHumanReadableName(){
        return name().replaceAll("_", " ");
    }

    public static Language fromCode(String code){
        //If "auto" choose language from current system, if language unavailable default to english
        if(code.equalsIgnoreCase("auto"))
            code = Locale.getDefault().toLanguageTag().replaceAll("-","");

        for(Language lang : values()){
            if(lang.getCode().equalsIgnoreCase(code))
                return lang;
        }

        Messenger.sendConsoleMessage("Valid language code not found. Loading up english locale");
        return Language.English;
    }
}
