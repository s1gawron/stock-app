package pl.eizodev.app.webScrape;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pl.eizodev.app.entity.Stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@NoArgsConstructor
public class StocksStats extends Stock {

    private static String getTickerFromWeb(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\" width=\"1%\"><b><a href=\"q/[?]s=[a-z0-9]{3}\">(.+?)</a></b></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return matcher.group(1);
        } catch (Exception e) {
            return "0";
        }
    }

    private static String getNameFromWeb(String body) {
        Pattern pattern = Pattern.compile(" {23}<td id=\"f10\" align=\"left\">(.+?)</td>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(body);
        matcher.find();
        return matcher.group(1);
    }

    private static float getPriceFromWeb(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\"><b><span id=\"aq_[a-z0-9]{3}_c[0-4]\">(.+?)</span></b></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return Float.parseFloat(matcher.group(1));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getPercentageChangeFromWeb(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\" nowrap><b><span id=\"aq_[a-z0-9]{3}_m1\"><span id=\"c[0-4]\">(.+?)</span></span></b></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return matcher.group(1);
        } catch (Exception e) {
            return "0";
        }
    }

    private static float getPriceChangeFromWeb(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\" nowrap><span id=\"aq_[a-z0-9]{3}_m2\"><span id=\"c[0-4]\">(.+?)</span></span></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return Float.parseFloat(matcher.group(1));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String getVolumeFromWeb(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\"><span id=\"aq_[a-z0-9]{3}_v2\">(.+?)</span></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return matcher.group(1);
        } catch (Exception e) {
            return "0";
        }
    }

    private static String getUpdateDateFromWeb(String body) {
        try {
            Pattern pattern = Pattern.compile(" {23}<td id=\"f13\" nowrap><span id=\"aq_[a-z0-9]{3}_t2\">(.+?)</span></td>", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(body);
            matcher.find();
            return matcher.group(1);
        } catch (Exception e) {
            return "0";
        }
    }

    public List<Stock> getAllStocksFromGivenIndex(String index) {
        String connectionLink = null;

        if (index.equals("WIG20")) {
            connectionLink = "https://stooq.pl/t/?i=532";
        } else if (index.equals("WIG40")) {
            connectionLink = "https://stooq.pl/t/?i=533";
        } else if (index.equals("WIG80")) {
            connectionLink = "https://stooq.pl/t/?i=588";
        }// else if (index.equals("ETF")) {
//            connectionLink = "https://stooq.pl/t/?i=606";
//        }

        List<Stock> stocks = new ArrayList<>();

        try {
            final Document doc = Jsoup.connect(connectionLink).get();
            String[] body = doc.body().html().split("\n");

            for (int i = 0; i < body.length; i++) {
                if (Pattern.matches(" {22}<tr id=\"r_[0-9]{1,2}\">", body[i])) {
                    String ticker = getTickerFromWeb(body[++i]);
                    String name = getNameFromWeb(body[++i]);
                    float price = getPriceFromWeb(body[++i]);
                    String changePerc = getPercentageChangeFromWeb(body[++i]);
                    float priceChange = getPriceChangeFromWeb(body[++i]);
                    String volume = getVolumeFromWeb(body[++i]);
                    String date = getUpdateDateFromWeb(body[++i]);
                    stocks.add(new Stock(index, ticker, name, price, changePerc, volume));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stocks;
    }
}