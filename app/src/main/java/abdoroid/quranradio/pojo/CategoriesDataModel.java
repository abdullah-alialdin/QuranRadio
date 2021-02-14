package abdoroid.quranradio.pojo;

public class CategoriesDataModel {
    private final String title;
    private final String description;
    private final int imageSource;
    private final String queryString;

    public CategoriesDataModel(String title, String description, int imageSource, String queryString){
        this.title = title;
        this.description = description;
        this.imageSource = imageSource;
        this.queryString = queryString;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getImageSource() {
        return imageSource;
    }

    public String getQueryString() {
        return queryString;
    }
}
