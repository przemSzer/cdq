package com.cdq.rag;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import dev.langchain4j.data.document.DocumentLoader;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.source.UrlSource;
import dev.langchain4j.data.document.transformer.jsoup.HtmlToTextDocumentTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class DownloadWebPageTool {

    private final Logger logger = LoggerFactory.getLogger(DownloadWebPageTool.class.getName());
    private final ApacheTikaDocumentParser parser;
    private final HtmlToTextDocumentTransformer transformer;

    public DownloadWebPageTool() {
        transformer = new HtmlToTextDocumentTransformer(null, null, true);
        parser = new ApacheTikaDocumentParser(true);
    }

    @Tool("Downloads a web page. The page is converted to text. Returns the text of the web page or error message in case of failure.")
    public String downloadWebPage(@P(value = "The URL of the web page to download" , name = "URL") String url, @ToolMemoryId String toolMemoryId){
        logger.info("Downloading web page: {}, tool memory id: {}", url, toolMemoryId);
        try {
            var asUrl = new URI(url);
            var urlDocumentSource = new UrlSource(asUrl.toURL());
            var document = DocumentLoader.load(urlDocumentSource, parser);
            var text = transformer.transform(document);
            return text.text();
        } catch (Exception e) {
            logger.error("Failed to download web page: {}, tool memory id: {}", url, toolMemoryId, e);
            var message = e.getMessage();
            if (e.getCause() instanceof Exception cause) {
                message += ", " + cause.getMessage();
            }
            return "Failed to download web page, the following error occurred: " + message;
        }
    }
}
