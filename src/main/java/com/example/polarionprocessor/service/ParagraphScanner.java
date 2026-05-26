package com.example.polarionprocessor.service;

import com.example.polarionprocessor.model.ParagraphInfo;
import com.example.polarionprocessor.util.HashUtils;
import com.example.polarionprocessor.util.TextUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParagraphScanner {

    public List<ParagraphInfo> scan(String htmlContent) {
        try {
            Document document = Jsoup.parseBodyFragment(htmlContent == null ? "" : htmlContent);
            document.outputSettings()
                    .prettyPrint(false)
                    .syntax(Document.OutputSettings.Syntax.html);

            Elements paragraphs = document.select("p");
            List<ParagraphInfo> results = new ArrayList<ParagraphInfo>();
            int seq = 1;
            for (Element paragraph : paragraphs) {
                String paragraphId = paragraph.id();
                String sourceText = TextUtils.normalizeSpaces(paragraph.text());
                String sourceOuterHtml = findOriginalOuterHtml(htmlContent, paragraphId);
                if (!TextUtils.hasText(sourceOuterHtml)) {
                    sourceOuterHtml = paragraph.outerHtml();
                }

                ParagraphInfo info = new ParagraphInfo();
                info.setSeq(seq++);
                info.setParagraphId(paragraphId);
                info.setSourceText(sourceText);
                info.setOutlineNo(TextUtils.extractOutlineNo(sourceText));
                info.setSourceOuterHtml(sourceOuterHtml);
                info.setSourceTextHash(HashUtils.sha256(sourceText));
                info.setSourceOuterHtmlHash(HashUtils.sha256(sourceOuterHtml));
                results.add(info);
            }
            return results;
        } catch (RuntimeException e) {
            throw new ModuleProcessException("HTML_PARSE_FAILED", "HTML parse failed", e);
        }
    }

    private String findOriginalOuterHtml(String htmlContent, String paragraphId) {
        if (!TextUtils.hasText(htmlContent) || !TextUtils.hasText(paragraphId)) {
            return null;
        }
        Pattern pattern = Pattern.compile(
                "(?s)<p\\b(?=[^>]*\\bid=[\"']" + Pattern.quote(paragraphId) + "[\"'])[^>]*>.*?</p>");
        Matcher matcher = pattern.matcher(htmlContent);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group();
    }
}
