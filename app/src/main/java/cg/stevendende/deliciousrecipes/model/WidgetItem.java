package cg.stevendende.deliciousrecipes.model;

/**
 * Created by STEVEN on 10/08/2017.
 */

public class WidgetItem {

    public String quantity;
    public String measure;
    public String ingredient;

    public WidgetItem(String quantity, String measure, String ingredient) {
        this.quantity = quantity;
        this.measure = measure;
        this.ingredient = ingredient;
    }
}
