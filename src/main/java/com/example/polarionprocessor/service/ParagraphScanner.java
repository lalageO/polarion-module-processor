package com.example.polarionprocessor.service;

import com.example.polarionprocessor.model.ParagraphInfo;
import com.example.polarionprocessor.util.HashUtils;
import com.example.polarionprocessor.util.TextUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParagraphScanner {

    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("(?is)<p\\b[^>]*>.*?</p>");

    public List<ParagraphInfo> scan(String htmlContent) {
        try {
            List<ParagraphInfo> results = new ArrayList<ParagraphInfo>();
            Matcher matcher = PARAGRAPH_PATTERN.matcher(htmlContent == null ? "" : htmlContent);
            int seq = 1;
            while (matcher.find()) {
                String sourceOuterHtml = matcher.group();
                Element paragraph = Jsoup.parseBodyFragment(sourceOuterHtml).selectFirst("p");
                if (paragraph == null) {
                    continue;
                }
                String paragraphId = paragraph.id();
                String sourceText = TextUtils.normalizeSpaces(paragraph.text());

                ParagraphInfo info = new ParagraphInfo();
                info.setSeq(seq++);
                info.setParagraphId(paragraphId);
                info.setSourceText(sourceText);
                info.setOutlineNo(TextUtils.extractOutlineNo(sourceText));
                info.setSourceOuterHtml(sourceOuterHtml);
                info.setSourceTextHash(HashUtils.sha256(sourceText));
                info.setSourceOuterHtmlHash(HashUtils.sha256(sourceOuterHtml));
                info.setSourceStartIndex(matcher.start());
                info.setSourceEndIndex(matcher.end());
                results.add(info);
            }
            return results;
        } catch (RuntimeException e) {
            throw new ModuleProcessException("HTML_PARSE_FAILED", "HTML parse failed", e);
        }
    }
}
