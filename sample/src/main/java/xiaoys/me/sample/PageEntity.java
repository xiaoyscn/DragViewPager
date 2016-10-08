package xiaoys.me.sample;

/**
 * Created by xiaoys on 2016/10/8.
 */

public class PageEntity {

    private int name;
    private int bgColor;

    public PageEntity() {
    }

    public PageEntity(int name, int bgColor) {
        this.name = name;
        this.bgColor = bgColor;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getBgColor() {
        return bgColor;
    }

    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }
}
