package test;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GeneratePDF {

    /*字体-黑体*/
    private static final String FONT = "simhei.ttf";
    /*HTML模板*/
    private static final String HTML_TEMPLATE = "template.html";
    /*生成的PDF文件*/
    private static final String TARGET = "target.pdf";
    /*生成的印章文件*/
    private static final String STAMPER = "stamper_result.pdf";
    /*生成的水印文件*/
    public static final String WATERMARK = "watermark_result.pdf";
    /*印章图片*/
    public static final String STAMPER_IMG = "stamper.png";

    private static Configuration freemarkerCfg = new Configuration();;

    public static String matchDataToHtml(Map<String, Object> data, String htmlTmp) {
        Writer out = new StringWriter();
        try {
            // HTML头部内容写入out流
            out.write("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/><title>Title</title><style>body {font-family: SimHei;font-size: 14px;line-height: 150%;}td {height: 25px;}</style></head><body>\n");

            // 获取模板,并设置编码方式
            Template template = freemarkerCfg.getTemplate(htmlTmp);
            template.setOutputEncoding("UTF-8");
            // 合并数据模型与模板
            template.process(data, out); //将合并后的数据和模板写入到流中，这里使用的字符流
            template.process(data, out); //将合并后的数据和模板写入到流中，这里使用的字符流
            template.process(data, out); //将合并后的数据和模板写入到流中，这里使用的字符流

            // HTML末尾内容写入out流
            out.write("</body></html>");

            out.flush();
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static void createPdf(String content) {
        try {
            ITextRenderer render = new ITextRenderer();
            ITextFontResolver fontResolver = render.getFontResolver();
            // 使用资源字体
            fontResolver.addFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            // HTML内容创建PDF
            render.setDocumentFromString(content);
            render.layout();
            render.createPDF(new FileOutputStream(TARGET));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void GenerateStamper() {
        try {
            PdfReader pdfReader = new PdfReader(new FileInputStream(TARGET));
            PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(STAMPER));
            Image image = Image.getInstance(STAMPER_IMG);
            PdfReaderContentParser parser = new PdfReaderContentParser(pdfReader);
            int pageSize = pdfReader.getNumberOfPages();
            KeywordListener keywordListener = new KeywordListener();
            keywordListener.setKeyWord("重复打印");
            ArrayList<KeywordListener.Coordinate> coordinateList = new ArrayList();
            for (int pageNumber = 1; pageNumber <= pageSize; pageNumber++) {
                // 获取某页的关键字位置
                keywordListener.init(pageNumber);
                parser.processContent(pageNumber, keywordListener);
                coordinateList.addAll(keywordListener.getCoordinates());
            }

            for (KeywordListener.Coordinate coordinate : coordinateList) {
                // 获取操作的页面
                PdfContentByte pdfContentByte = pdfStamper.getOverContent(coordinate.getPage());
                // 根据域的大小缩放图片
                image.scaleAbsolute(80f, 80f);
                // 添加图片
                image.setAbsolutePosition(coordinate.getX(), coordinate.getY());
                pdfContentByte.addImage(image);
            }

            pdfStamper.close();
            pdfReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void GenerateWaterMark() throws IOException, DocumentException {

        PdfReader pdfReader = new PdfReader(new FileInputStream(TARGET));
        PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(WATERMARK));
        // 原pdf文件的总页数
        int pageSize = pdfReader.getNumberOfPages();
        // 设置字体
        BaseFont font = BaseFont.createFont(FONT, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        // 设置填充字体不透明度为0.1f
        PdfGState gs = new PdfGState();
        gs.setFillOpacity(0.2f);
        Document document = new Document();
        float documentWidth = document.getPageSize().getWidth(), documentHeight = document.getPageSize().getHeight();
        final float xStart = 0, yStart = 0, xInterval = 40, yInterval = 40, rotation = 45, fontSize = 18;
        String watermarkWord = "水印";
        int red = 128, green = 128, blue = 128;

        for (int i = 1; i <= pageSize; i++) {
            // 水印在之前文本下
            PdfContentByte pdfContentByte = pdfStamper.getUnderContent(i);
            pdfContentByte.beginText();
            // 文字水印 颜色
            pdfContentByte.setColorFill(new BaseColor(red, green, blue));
            // 文字水印 字体及字号
            pdfContentByte.setFontAndSize(font, fontSize);
            pdfContentByte.setGState(gs);
            // 文字水印 起始位置
            pdfContentByte.setTextMatrix(xStart, yStart);

            for (float x = xStart; x <= documentWidth + xInterval; x += xInterval) {
                for (float y = yStart; y <= documentHeight + yInterval; y += yInterval) {
                    pdfContentByte.showTextAligned(Element.ALIGN_CENTER, watermarkWord, x, y, rotation);
                }
            }
            pdfContentByte.endText();
        }
        // 关闭
        pdfStamper.close();
        pdfReader.close();
    }

    public static void main(String[] args) throws IOException, DocumentException {
        Map<String, Object> data = new HashMap();
        data.put("date",new Date());
        data.put("payName", "重庆大学");
        /*1.生成模板PDF文件*/
        // 模板HTML+Data数据
        String content = GeneratePDF.matchDataToHtml(data, HTML_TEMPLATE);
        // 创建PDF文件
        GeneratePDF.createPdf(content);
        /*2.对生成的文件进行印章*/
        GenerateStamper();
        /*3.对生成的文件添加水印*/
        GenerateWaterMark();
    }


}