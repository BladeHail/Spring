package com.example.first.controller;

import com.example.first.service.LiveYtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.w3c.dom.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/yt")
public class YtCallbackController {

    private final LiveYtService liveYtService;

    /**
     * WebSub 구독 검증용 엔드포인트 (GET).
     * Google Hub가 hub.challenge 를 보내면 그대로 응답해야 한다.
     */
    @GetMapping("/callback")
    public ResponseEntity<String> verifyCallback(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.challenge") String challenge,
            @RequestParam(value = "hub.topic", required = false) String topic
    ) {
        System.out.println("[YouTube Callback] VERIFY: mode=" + mode + ", topic=" + topic);
        // 반드시 challenge 그대로 반환
        return ResponseEntity.ok(challenge);
    }

    /**
     * WebSub 이벤트 수신용 엔드포인트 (POST).
     * YouTube 채널에서 새로운 영상/라이브가 발생할 때 XML payload가 도착한다.
     */
    @PostMapping("/callback")
    public ResponseEntity<Void> receiveEvent(@RequestBody String xmlBody) {
        System.out.println("[YouTube Callback] EVENT RECEIVED:");
        System.out.println(xmlBody);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(
                    new ByteArrayInputStream(xmlBody.getBytes(StandardCharsets.UTF_8))
            );

            NodeList entryNodes = doc.getElementsByTagName("entry");
            for (int i = 0; i < entryNodes.getLength(); i++) {
                Element entry = (Element) entryNodes.item(i);

                String title = getFirstChildText(entry, "title");
                String videoId = getFirstChildText(entry, "yt:videoId");
                String channelId = getFirstChildText(entry, "yt:channelId");

                if (videoId != null && !videoId.isBlank()
                        && channelId != null && !channelId.isBlank()) {

                    // title에 키워드가 있는지 / 실제 라이브인지 여부는 Service에서 처리
                    liveYtService.registerLiveFromCallback(title, videoId, channelId);
                }
            }

        } catch (Exception e) {
            e.getStackTrace();
        }

        return ResponseEntity.ok().build();
    }

    private String getFirstChildText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0) return null;
        Node node = list.item(0);
        return node.getTextContent();
    }
}
