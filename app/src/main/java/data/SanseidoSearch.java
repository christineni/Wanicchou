package data;

import android.net.Uri;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Limegrass on 3/19/2018.
 */

public class SanseidoSearch {

    final static String SANSEIDOU_BASE_URL = "https://www.sanseido.biz/User/Dic/Index.aspx";
    final static String PARAM_WORD_QUERY = "TWords";

    // Order of dictionaries under select dictionaries
    // First is the one that displays
    // 15 = J-J
    // 16 = E-J
    // 17 = J-E
    final static String PARAM_DORDER= "DORDER";
    final static String DORDER_DEFAULT = "171615";

    // ST is the match preference.
    // 0 = Forward match
    // 1 = Exact
    // 2 = Backwards
    // 3 = Full text search
    // 5 = Partial match
    final static String PARAM_ST = "st";
    final static String ST_DEFAULT = "1";

    // Enabling and disabling of languages
    // Display will go by DORDER
    final static String PARAM_DAILYJJ = "DailyJJ";
    final static String PARAM_DAILYJE = "DailyJE";
    final static String PARAM_DAILYEJ = "DailyEJ";
    final static String SET_LANG = "checkbox";

    final static String SANSEIDO_WORD_ID = "word";
    final static String SANSEIDO_WORD_DEFINITION_ID = "wordBody";

    //TODO: Maybe something more graceful or renaming
    final static String MULTIPLE_DEFINITION_REGEX = "▼";
    final static String MULTIPLE_DEFINITION_SEPARATOR = "\n▼";

    /**
     * Builds the URL for the desired word(s) to search for on Sanseido.
     *
     * @param word the Japanese word to search for
     * @param JJDic whether to return the definition in Japanese or not (else, English)
     * @return the Sanseido url created
     * @throws MalformedURLException
     */
    public static URL buildQueryURL(String word, boolean JJDic) throws MalformedURLException{

        Uri.Builder uriBuilder = Uri.parse(SANSEIDOU_BASE_URL).buildUpon()
                        .appendQueryParameter(PARAM_ST, ST_DEFAULT)
                        .appendQueryParameter(PARAM_DORDER, DORDER_DEFAULT)
                        .appendQueryParameter(PARAM_WORD_QUERY, word);
        if(JJDic){
            uriBuilder.appendQueryParameter(PARAM_DAILYJJ, SET_LANG);
        }
        else{
            uriBuilder.appendQueryParameter(PARAM_DAILYJE, SET_LANG);
        }

        return new URL(uriBuilder.build().toString());
    }

    public static Document getSanseidoSource(URL searchURL) throws IOException{
        Document htmlTree = Jsoup.connect(searchURL.toString()).get();

        return htmlTree;
    }

    // Maybe unnecessary and just catch no words before this point
    public static String getWord(Document htmlTree){
        Element word = htmlTree.getElementById(SANSEIDO_WORD_ID);

        return word.text().toString();
    }

    public static String getDefinition(Document htmlTree){
        Element definitionParentElement = htmlTree.getElementById(SANSEIDO_WORD_DEFINITION_ID);
        // The definition is in a further div, single child
        String definition = "";

        if(definitionParentElement.children().size() > 0){
            definition = definitionParentElement.child(0).text();
        }

        //TODO: FIX REGEX
        definition = definition.replaceAll(MULTIPLE_DEFINITION_REGEX, MULTIPLE_DEFINITION_SEPARATOR);
        definition = definition.replaceFirst(MULTIPLE_DEFINITION_REGEX, MULTIPLE_DEFINITION_SEPARATOR);

        return definition;
    }

    //TODO: Create links to words in the related words table

    //TODO: Isolate the Kanji without kana
    //Use as part to getFurigana, likely
    public static String getKanji(String word){
        return "";
    }
    //TODO: Method to return Furigana in Anki format as string
    // Could use J-E dic for consistency instead of scraping.
    public static String getFurigana(String word){
        return "";
    }

    //TODO: Isolate readings
    public static String getReading(String word){
        return "";
    }

    //TODO: Separate and format each definition
    public static String[] getDefinitions(String definitions){
        //Many are separated by ▼, so can use that to split the string or as a regex.
        return null;
    }

}
