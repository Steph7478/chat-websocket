package com.app.security.ratelimiter.filters;
// package com.chat.ratelimiter.filters;

// import org.springframework.stereotype.Component;
// import org.springframework.web.server.ServerWebExchange;
// import org.springframework.web.server.WebFilter;
// import org.springframework.web.server.WebFilterChain;
// import reactor.core.publisher.Mono;

// import java.net.InetSocketAddress;
// import java.security.Principal;
// import java.time.Instant;

// @Component
// public class LogHeadersFilter implements WebFilter {

// @Override
// public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

// long startTime = System.currentTimeMillis();

// System.out.println("\n================= 🔍 INCOMING REQUEST
// =================");

// /*
// * =====================================================
// * 1️⃣ IDENTIDADE DA REQUEST (Tracing / Observabilidade)
// * =====================================================
// */
// System.out.println("Request ID: " + exchange.getRequest().getId());
// System.out.println("Timestamp: " + Instant.now());

// /*
// * =========================
// * 2️⃣ HTTP CORE
// * =========================
// */
// System.out.println("Method: " + exchange.getRequest().getMethod());
// System.out.println("URI: " + exchange.getRequest().getURI());
// System.out.println("Path: " + exchange.getRequest().getPath());
// System.out.println("Raw Path: " + exchange.getRequest().getPath().value());
// System.out.println("Query Params: " +
// exchange.getRequest().getQueryParams());

// /*
// * =========================
// * 3️⃣ HEADERS COMPLETOS
// * =========================
// */
// System.out.println("\n--- HEADERS ---");
// exchange.getRequest().getHeaders().forEach((key, values) -> {
// System.out.println(key + " = " + values);
// });

// /*
// * =========================
// * 4️⃣ COOKIES
// * =========================
// */
// System.out.println("\n--- COOKIES ---");
// exchange.getRequest().getCookies().forEach((name, cookies) -> {
// System.out.println(name + " = " + cookies);
// });

// /*
// * =========================
// * 5️⃣ REDE / IP / PROXY
// * =========================
// */
// InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
// System.out.println("\n--- NETWORK ---");
// System.out.println("Remote Address: " + remoteAddress);
// System.out.println("Remote IP: " + (remoteAddress != null ?
// remoteAddress.getAddress() : "null"));
// System.out.println("Remote Port: " + (remoteAddress != null ?
// remoteAddress.getPort() : "null"));

// System.out.println("X-Forwarded-For: " +
// exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"));
// System.out.println("X-Real-IP: " +
// exchange.getRequest().getHeaders().getFirst("X-Real-IP"));
// System.out.println("Forwarded: " +
// exchange.getRequest().getHeaders().getFirst("Forwarded"));

// /*
// * =========================
// * 6️⃣ SEGURANÇA / CONTEXTO
// * =========================
// */
// System.out.println("\n--- SECURITY ---");
// System.out.println("Origin: " +
// exchange.getRequest().getHeaders().getFirst("Origin"));
// System.out.println("Referer: " +
// exchange.getRequest().getHeaders().getFirst("Referer"));

// boolean hasAuth =
// exchange.getRequest().getHeaders().getFirst("Authorization") != null;

// System.out.println("Authorization present: " + hasAuth);

// System.out.println("Has Cookies: " +
// !exchange.getRequest().getCookies().isEmpty());

// /*
// * =========================
// * 7️⃣ HEADERS MODERNOS (ANTI-CSRF / ANTI-BOT)
// * =========================
// */
// System.out.println("\n--- FETCH METADATA ---");
// System.out.println("Sec-Fetch-Site: " +
// exchange.getRequest().getHeaders().getFirst("Sec-Fetch-Site"));
// System.out.println("Sec-Fetch-Mode: " +
// exchange.getRequest().getHeaders().getFirst("Sec-Fetch-Mode"));
// System.out.println("Sec-Fetch-Dest: " +
// exchange.getRequest().getHeaders().getFirst("Sec-Fetch-Dest"));
// System.out.println("Sec-Fetch-User: " +
// exchange.getRequest().getHeaders().getFirst("Sec-Fetch-User"));

// /*
// * =========================
// * 8️⃣ CLIENT FINGERPRINT
// * =========================
// */
// System.out.println("\n--- CLIENT ---");
// System.out.println("User-Agent: " +
// exchange.getRequest().getHeaders().getFirst("User-Agent"));
// System.out.println("Accept: " +
// exchange.getRequest().getHeaders().getFirst("Accept"));
// System.out.println("Accept-Language: " +
// exchange.getRequest().getHeaders().getFirst("Accept-Language"));
// System.out.println("Accept-Encoding: " +
// exchange.getRequest().getHeaders().getFirst("Accept-Encoding"));

// /*
// * =========================
// * 9️⃣ CONTEÚDO / PAYLOAD (sem ler body)
// * =========================
// */
// System.out.println("\n--- CONTENT ---");
// System.out.println("Content-Type: " +
// exchange.getRequest().getHeaders().getContentType());
// System.out.println("Content-Length: " +
// exchange.getRequest().getHeaders().getContentLength());

// /*
// * =========================
// * 🔟 CONTEXTO REATIVO
// * =========================
// */
// System.out.println("\n--- REACTIVE CONTEXT ---");
// exchange.getAttributes().forEach((k, v) -> {
// System.out.println("Attribute: " + k + " = " + v);
// });

// /*
// * =========================
// * 1️⃣1️⃣ PRINCIPAL (AUTH CONTEXT)
// * =========================
// */
// Mono<Principal> principalMono = exchange.getPrincipal();
// principalMono.subscribe(principal -> {
// if (principal != null) {
// System.out.println("Principal Name: " + principal.getName());
// } else {
// System.out.println("Principal: null");
// }
// });

// System.out.println("=======================================================\n");

// return chain.filter(exchange)
// .doFinally(signal -> {
// long duration = System.currentTimeMillis() - startTime;
// System.out.println("⏱ Request processed in " + duration + "ms");
// });
// }
// }
