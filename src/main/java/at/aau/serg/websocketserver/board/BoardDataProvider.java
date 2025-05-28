package at.aau.serg.websocketserver.board;

import java.util.List;

/**
 * Schnittstelle für den Zugriff auf die Spielbrettdaten.
 * Diese Schnittstelle ermöglicht das einfache Testen durch Dependency Injection
 * und Mock-Implementierungen.
 */
public interface BoardDataProvider {
    
    /**
     * Liefert die Liste aller Felder des Spielbretts.
     *
     * @return Eine unveränderbare Liste aller Felder
     */
    List<Field> getBoard();
    
    /**
     * Liefert ein bestimmtes Feld anhand seines Index.
     *
     * @param index Der Index des gesuchten Felds
     * @return Das gefundene Feld oder null, wenn kein Feld mit diesem Index existiert
     */
    Field getFieldByIndex(int index);
}
