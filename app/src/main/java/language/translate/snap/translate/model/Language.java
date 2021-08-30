package language.translate.snap.translate.model;

public class Language {

    private String code;
    private String displayName;

    public Language(String str, String str2) {
        this.displayName = str;
        this.code = str2;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String str) {
        this.displayName = str;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String str) {
        this.code = str;
    }

}
