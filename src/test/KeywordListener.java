package test;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import java.util.ArrayList;


public class KeywordListener implements RenderListener {

    private ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();
    private String keyWord;
    private int page;

    public void init(int pageNumber) {
        this.page = pageNumber;
        this.coordinates.clear();
    }

    public ArrayList<Coordinate> getCoordinates() {
        return coordinates;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Override
    public void beginTextBlock() {
    }

    @Override
    public void endTextBlock() {
    }

    @Override
    public void renderImage(ImageRenderInfo arg0) {
    }

    @Override
    public void renderText(TextRenderInfo textRenderInfo) {
        String text = textRenderInfo.getText();
        if (null != text && text.contains(this.keyWord)) {
            Rectangle2D.Float boundingRectange = textRenderInfo.getBaseline().getBoundingRectange();
            Coordinate coordinate = new Coordinate();
            coordinate.setX(boundingRectange.x + 320);
            coordinate.setY(boundingRectange.y);
            coordinate.setPage(this.page);
            coordinates.add(coordinate);
        }
    }


    class Coordinate {
        public float x;
        public float y;
        public int page;

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }
    }
}
