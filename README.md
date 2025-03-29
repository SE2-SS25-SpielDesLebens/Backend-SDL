🎮 WebSocket-Server für Spiel des Lebens – Überblick
Wir haben einen eigenen Server mit STOMP-basierter WebSocket-Kommunikation implementiert. Dieser dient als Kommunikationsschnittstelle zwischen den Spieler:innen der Android-App. Jeder Spielerzug, Chat oder Schummelversuch wird darüber synchronisiert.

🚀 Was kann der Server aktuell?
Funktion	Beschreibung
✅ Spielzüge übertragen	Spieler:innen senden ihren Zug an den Server – dieser broadcastet an alle
✅ Chat zwischen Spieler:innen	Chatnachrichten werden über WebSocket verteilt
✅ STOMP-Protokoll	Für strukturierte Kommunikation mit @MessageMapping
✅ SockJS-Fallback	Auch auf Geräten ohne echten WebSocket-Support nutzbar
❌ Persistenz	(noch nicht – kann aber später ergänzt werden)
❌ Authentifizierung	(optional nachrüstbar)


⚙️ So funktioniert es technisch
📡 Verbindung:
Die Clients (unsere Android-App) verbinden sich mit dem Server über:
/websocket-example-broker

📍 Entspricht in der App:

val sockJsClient = SockJSClient(...)
val stompClient = Stomp.over(sockJsClient)
stompClient.connect("ws://<SERVER-IP>:8080/websocket-example-broker", ...)

🔁 Nachrichtenfluss:
1. Client sendet z. B. Spielzug an:
/app/move

2. Der Server empfängt über:
@MessageMapping("/move")

3. Der Server sendet die Antwort an alle:
/topic/game

4. Alle Clients, die /topic/game abonniert haben, bekommen die Nachricht automatisch.


📦 Datenformate (DTOs)
📨 Vom Client gesendet: StompMessage

{
  "playerName": "Anna",
  "action": "würfelt 6",
  "messageText": ""
}

📤 Vom Server zurück: OutputMessage
{
  "playerName": "Anna",
  "content": "würfelt 6",
  "timestamp": "2025-03-29T12:34:56"
}



🛠️ Wie ihr den Server lokal startet
Projekt klonen:

git clone https://github.com/SE2-SS25-SpielDesLebens/Backend-SDL.git
In IntelliJ öffnen

Starte die Application.kt oder Application.java (Spring Boot)

Der Server läuft unter:

http://localhost:8080
