package at.aau.serg.websocketserver.session.board;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Standardimplementierung des BoardDataProvider-Interfaces.
 * Diese Klasse enthält die statischen Daten des Spielbretts.
 */
@Component
public class BoardData implements BoardDataProvider {    private static final List<Field> BOARD = Arrays.asList(
        // x größer ist rechts und y größer ist runter
        //StartNormal
        new Field(1, 0.09, 0.63, Arrays.asList(2,3,4,5,6,7,8,9,20,21), FieldType.STARTNORMAL),
        new Field(2, 0.145, 0.6205, Arrays.asList(3,4,5,6,7,8,9,20,21,22), FieldType.ZAHLTAG),
        new Field(3, 0.18, 0.6, Arrays.asList(4,5,6,7,8,9,20,21,22,23), FieldType.AKTION),
        new Field(4, 0.2105, 0.571, Arrays.asList(5,6,7,8,9,20,21,22,23,24), FieldType.ANLAGE),
        new Field(5, 0.243, 0.598, Arrays.asList(6,7,8,9,20,21,22,23,24,25), FieldType.AKTION),
        new Field(6, 0.275, 0.571, Arrays.asList(7,8,9,20,21,22,23,24,25,26), FieldType.FREUND),
        new Field(7, 0.308, 0.548, Arrays.asList(8,9,20,21,22,23,24,25,26,27), FieldType.AKTION),
        new Field(8, 0.339, 0.57, Arrays.asList(9,20,21,22,23,24,25,26,27,28), FieldType.BERUF),
        new Field(9, 0.3715, 0.599, Arrays.asList(20,21,22,23,24,25,26,27,28), FieldType.AKTION),

        //StartUni
        new Field(10, 0.09, 0.795, Arrays.asList(11,12,13,14,15,16,17,18,19), FieldType.STARTUNI),
        new Field(11, 0.145, 0.785, Arrays.asList(12,13,14,15,16,17,18,19), FieldType.AKTION),
        new Field(12, 0.178, 0.806, Arrays.asList(13,14,15,16,17,18,19), FieldType.FREUND),
        new Field(13, 0.2102, 0.788, Arrays.asList(14,15,16,17,18,19), FieldType.AKTION),
        new Field(14, 0.243, 0.7605, Arrays.asList(15,16,17,18,19), FieldType.FREUND),
        new Field(15, 0.275, 0.735, Arrays.asList(16,17,18,19), FieldType.AKTION),
        new Field(16, 0.308, 0.712, Arrays.asList(17,18,19), FieldType.AKTION),
        new Field(17, 0.339, 0.738, Arrays.asList(18,19), FieldType.FREUND),
        new Field(18, 0.372, 0.71, Arrays.asList(19), FieldType.AKTION),
        new Field(19, 0.405, 0.68, Arrays.asList(20,21,22,23,24,25,26,27,28), FieldType.EXAMEN),
        
        //Gemeinsamer Weg zur Heirat
        new Field(20, 0.405, 0.62, Arrays.asList(21,22,23,24,25,26,27,28), FieldType.ZAHLTAG),
        new Field(21, 0.435, 0.595, Arrays.asList(22,23,24,25,26,27,28), FieldType.AKTION),
        new Field(22, 0.47, 0.62, Arrays.asList(23,24,25,26,27,28), FieldType.HAUS),
        new Field(23, 0.5, 0.595, Arrays.asList(24,25,26,27,28), FieldType.AKTION),
        new Field(24, 0.5, 0.548, Arrays.asList(25,26,27,28), FieldType.ZAHLTAG),
        new Field(25, 0.47, 0.523, Arrays.asList(26,27,28), FieldType.AKTION),
        new Field(26, 0.435, 0.498, Arrays.asList(27,28), FieldType.FREUND),
        new Field(27, 0.405, 0.473, Arrays.asList(28), FieldType.AKTION),
        new Field(28, 0.37, 0.44, Arrays.asList(29), FieldType.HEIRAT),
        
        // Heirat Ja
        new Field(29, 0.37, 0.44, Arrays.asList(30,31,32,33,34,35,36,37,44,45), FieldType.HEIRAT_JA),
        new Field(30, 0.335, 0.445, Arrays.asList(31,32,33,34,35,36,37,44,45,46), FieldType.AKTION),
        new Field(31, 0.305, 0.465, Arrays.asList(32,33,34,35,36,37,44,45,46,47), FieldType.AKTION),
        new Field(32, 0.275, 0.49, Arrays.asList(33,34,35,36,37,44,45,46,47), FieldType.AKTION),
        new Field(33, 0.245, 0.49, Arrays.asList(34,35,36,37,44,45,46,47), FieldType.ZAHLTAG),
        new Field(34, 0.215, 0.463, Arrays.asList(35,36,37,44,45,46,47), FieldType.AKTION),
        new Field(35, 0.18, 0.435, Arrays.asList(36,37,44,45,46,47), FieldType.HAUS),
        new Field(36, 0.18, 0.385, Arrays.asList(37,44,45,46,47), FieldType.AKTION),
        new Field(37, 0.18, 0.333, Arrays.asList(44,45,46,47), FieldType.ANLAGE),
        
        // Heirat Nein
        new Field(38, 0.37, 0.44, Arrays.asList(39,40,41,42,43,44,45,46,47), FieldType.HEIRAT_NEIN),
        new Field(39, 0.373, 0.39, Arrays.asList(40,41,42,43,44,45,46,47), FieldType.BERUF),
        new Field(40, 0.34, 0.36, Arrays.asList(41,42,43,44,45,46,47), FieldType.AKTION),
        new Field(41, 0.31, 0.335, Arrays.asList(42,43,44,45,46,47), FieldType.ZAHLTAG),
        new Field(42, 0.275, 0.31, Arrays.asList(43,44,45,46,47), FieldType.AKTION),
        new Field(43, 0.245, 0.335, Arrays.asList(44,45,46,47), FieldType.AKTION),
        
        // Gemeinsamer Weg zum Kind
        new Field(44, 0.213, 0.305, Arrays.asList(45,46,47), FieldType.AKTION),
        new Field(45, 0.213, 0.255, Arrays.asList(46,47), FieldType.HAUS),
        new Field(46, 0.245, 0.23, Arrays.asList(47), FieldType.AKTION),
        new Field(47, 0.285, 0.23, Arrays.asList(48), FieldType.KINDER),
        
        // Kinder Ja
        new Field(48, 0.285, 0.23, Arrays.asList(49,50,51,52,53,54,55,56,57,58), FieldType.KINDER_JA),
        new Field(49, 0.315, 0.265, Arrays.asList(50,51,52,53,54,55,56,57,58,59), FieldType.AKTION),
        new Field(50, 0.345, 0.29, Arrays.asList(51,52,53,54,55,56,57,58,59,60), FieldType.BABY),
        new Field(51, 0.378, 0.312, Arrays.asList(52,53,54,55,56,57,58,59,60,61), FieldType.AKTION),
        new Field(52, 0.41, 0.29, Arrays.asList(53,54,55,56,57,58,59,60,61,71), FieldType.ZAHLTAG),
        new Field(53, 0.442, 0.315, Arrays.asList(54,55,56,57,58,59,60,61,71,72), FieldType.AKTION),
        new Field(54, 0.442, 0.363, Arrays.asList(55,56,57,58,59,60,61,71,72,73), FieldType.HAUS),
        new Field(55, 0.475, 0.388, Arrays.asList(56,57,58,59,60,61,71,72,73,74), FieldType.AKTION),
        new Field(56, 0.508, 0.413, Arrays.asList(57,58,59,60,61,71,72,73,74,75), FieldType.ZWILLINGE),
        new Field(57, 0.54, 0.388, Arrays.asList(58,59,60,61,71,72,73,74,75,76), FieldType.AKTION),
        new Field(58, 0.54, 0.342, Arrays.asList(59,60,61,71,72,73,74,75,76,77), FieldType.TIER),
        new Field(59, 0.54, 0.288, Arrays.asList(60,61,71,72,73,74,75,76,77,78), FieldType.AKTION),
        new Field(60, 0.57, 0.263, Arrays.asList(61,71,72,73,74,75,76,77,78,79), FieldType.AKTION),
        new Field(61, 0.57, 0.228, Arrays.asList(71,72,73,74,75,76,77,78,79,80), FieldType.BABY),

        // Kinder Nein
        new Field(62, 0.285, 0.23, Arrays.asList(63,64,65,66,67,68,69,70,71,72), FieldType.KINDER_NEIN),
        new Field(63, 0.315, 0.225, Arrays.asList(64,65,66,67,68,69,70,71,72,73), FieldType.AKTION),
        new Field(64, 0.345, 0.205, Arrays.asList(65,66,67,68,69,70,71,72,73,74), FieldType.BERUF),
        new Field(65, 0.376, 0.225, Arrays.asList(66,67,68,69,70,71,72,73,74,75), FieldType.AKTION),
        new Field(66, 0.41, 0.205, Arrays.asList(67,68,69,70,71,72,73,74,75,76), FieldType.ZAHLTAG),
        new Field(67, 0.443, 0.225, Arrays.asList(68,69,70,71,72,73,74,75,76,77), FieldType.AKTION),
        new Field(68, 0.475, 0.205, Arrays.asList(69,70,71,72,73,74,75,76,77,78), FieldType.ANLAGE),
        new Field(69, 0.505, 0.18, Arrays.asList(70,71,72,73,74,75,76,77,78,79), FieldType.TIER),
        new Field(70, 0.54, 0.152, Arrays.asList(71,72,73,74,75,76,77,78,79,80), FieldType.AKTION),
        
        //Gemeinsamer Weg zur Midlifechrisis
        new Field(71, 0.57, 0.173, Arrays.asList(72,73,74,75,76,77,78,79,80,81), FieldType.ZAHLTAG),
        new Field(72, 0.605, 0.152, Arrays.asList(73,74,75,76,77,78,79,80,81,82), FieldType.AKTION),
        new Field(73, 0.635, 0.173, Arrays.asList(74,75,76,77,78,79,80,81,82,83), FieldType.HAUS),
        new Field(74, 0.67, 0.202, Arrays.asList(75,76,77,78,79,80,81,82,83), FieldType.AKTION),
        new Field(75, 0.70, 0.228, Arrays.asList(76,77,78,79,80,81,82,83), FieldType.AKTION),
        new Field(76, 0.735, 0.252, Arrays.asList(77,78,79,80,81,82,83), FieldType.ZAHLTAG),
        new Field(77, 0.768, 0.278, Arrays.asList(78,79,80,81,82,83), FieldType.AKTION),
        new Field(78, 0.797, 0.303, Arrays.asList(79,80,81,82,83), FieldType.FREUND),
        new Field(79, 0.83, 0.327, Arrays.asList(80,81,82,83), FieldType.AKTION),
        new Field(80, 0.863, 0.353, Arrays.asList(81,82,83), FieldType.BERUF),
        new Field(81, 0.895, 0.378, Arrays.asList(82,83), FieldType.AKTION),
        new Field(82, 0.925, 0.405, Arrays.asList(83), FieldType.ANLAGE),
        new Field(83, 0.925, 0.465, Arrays.asList(84), FieldType.MIDLIFECHRISIS),
        
        // Midlifechrisis rot
        new Field(84, 0.925, 0.465, Arrays.asList(85,86,87,88,89,90,91,92,93,94), FieldType.MIDLIFECHRISIS_ROT),
        new Field(85, 0.895, 0.465, Arrays.asList(86,87,88,89,90,91,92,93,94,95), FieldType.ANLAGE),
        new Field(86, 0.86, 0.44, Arrays.asList(87,88,89,90,91,92,93,94,95,102), FieldType.AKTION),
        new Field(87, 0.83, 0.44, Arrays.asList(88,89,90,91,92,93,94,95,102,103), FieldType.BERUF),
        new Field(88, 0.797, 0.465, Arrays.asList(89,90,91,92,93,94,95,102,103,104), FieldType.AKTION),
        new Field(89, 0.765, 0.49, Arrays.asList(90,91,92,93,94,95,102,103,104,105), FieldType.HAUS),
        new Field(90, 0.765, 0.54, Arrays.asList(91,92,93,94,95,102,103,104,105,106), FieldType.AKTION),
        new Field(91, 0.797, 0.565, Arrays.asList(92,93,94,95,102,103,104,105,106,107), FieldType.ZAHLTAG),
        new Field(92, 0.797, 0.62, Arrays.asList(93,94,95,102,103,104,105,106,107,108), FieldType.AKTION),
        new Field(93, 0.83, 0.645, Arrays.asList(94,95,102,103,104,105,106,107,108,109), FieldType.TIER),
        new Field(94, 0.83, 0.695, Arrays.asList(95,102,103,104,105,106,107,108,109,110), FieldType.ANLAGE),
        new Field(95, 0.862, 0.722, Arrays.asList(102,103,104,105,106,107,108,109,110,111), FieldType.AKTION),
        
        // Midlifechrisis schwarz
        new Field(96, 0.925, 0.465, Arrays.asList(97,98,99,100,101,102,103,104,105,106), FieldType.MIDLIFECHRISIS_SCHWARZ),
        new Field(97, 0.895, 0.495, Arrays.asList(98,99,100,101,102,103,104,105,106,107), FieldType.AKTION),
        new Field(98, 0.895, 0.543, Arrays.asList(99,100,101,102,103,104,105,106,107,108), FieldType.ZAHLTAG),
        new Field(99, 0.895, 0.593, Arrays.asList(100,101,102,103,104,105,106,107,108,109), FieldType.AKTION),
        new Field(100, 0.895, 0.642, Arrays.asList(101,102,103,104,105,106,107,108,109,110), FieldType.FREUND),
        new Field(101, 0.895, 0.694, Arrays.asList(102,103,104,105,106,107,108,109,110,111), FieldType.AKTION),
        
        // Gemeinsamer Weg zum Ruhestand
        new Field(102, 0.895, 0.745, Arrays.asList(103,104,105,106,107,108,109,110,111,112), FieldType.ZAHLTAG),
        new Field(103, 0.895, 0.798, Arrays.asList(104,105,106,107,108,109,110,111,112,113), FieldType.AKTION),
        new Field(104, 0.863, 0.823, Arrays.asList(105,106,107,108,109,110,111,112,113,114), FieldType.ANLAGE),
        new Field(105, 0.83, 0.8, Arrays.asList(106,107,108,109,110,111,112,113,114,115), FieldType.AKTION),
        new Field(106, 0.8, 0.823, Arrays.asList(107,108,109,110,111,112,113,114,115), FieldType.HAUS),
        new Field(107, 0.765, 0.8, Arrays.asList(108,109,110,111,112,113,114,115), FieldType.AKTION),
        new Field(108, 0.733, 0.772, Arrays.asList(109,110,111,112,113,114,115), FieldType.BERUF),
        new Field(109, 0.7, 0.748, Arrays.asList(110,111,112,113,114,115), FieldType.ANLAGE),
        new Field(110, 0.67, 0.77, Arrays.asList(111,112,113,114,115), FieldType.ZAHLTAG),
        new Field(111, 0.645, 0.75, Arrays.asList(112,113,114,115), FieldType.AKTION),
        new Field(112, 0.605, 0.772, Arrays.asList(113,114,115), FieldType.FREUND),
        new Field(113, 0.575, 0.75, Arrays.asList(114,115), FieldType.AKTION),
        new Field(114, 0.575, 0.698, Arrays.asList(115), FieldType.AKTION),
        new Field(115, 0.575, 0.64, Arrays.asList(116), FieldType.FRUEHPENSION),
        
        // Frühpension Ja
        new Field(116, 0.575, 0.645, Arrays.asList(117,118,119), FieldType.FRUEHPENSION_JA),
        new Field(117, 0.61, 0.645, Arrays.asList(118,119), FieldType.AKTION),
        new Field(118, 0.64, 0.615, Arrays.asList(119), FieldType.ZAHLTAG),
        new Field(119, 0.665, 0.565, Arrays.asList(119), FieldType.RUHESTAND),
        
        // Frühpension Nein
        new Field(120, 0.575, 0.64, Arrays.asList(121,122,123,124,125,126,127,128,129,130), FieldType.FRUEHPENSION_NEIN),
        new Field(121, 0.575, 0.585, Arrays.asList(122,123,124,125,126,127,128,129,130,131), FieldType.AKTION),
        new Field(122, 0.575, 0.535, Arrays.asList(123,124,125,126,127,128,129,130,131,132), FieldType.BERUF),
        new Field(123, 0.604, 0.51, Arrays.asList(124,125,126,127,128,129,130,131,132,133), FieldType.ANLAGE),
        new Field(124, 0.604, 0.458, Arrays.asList(125,126,127,128,129,130,131,132,133,134), FieldType.TIER),
        new Field(125, 0.604, 0.407, Arrays.asList(126,127,128,129,130,131,132,133,134), FieldType.AKTION),
        new Field(126, 0.635, 0.378, Arrays.asList(127,128,129,130,131,132,133,134), FieldType.ZAHLTAG),
        new Field(127, 0.668, 0.35, Arrays.asList(128,129,130,131,132,133,134), FieldType.AKTION),
        new Field(128, 0.7, 0.325, Arrays.asList(129,130,131,132,133,134), FieldType.HAUS),
        new Field(129, 0.735, 0.35, Arrays.asList(130,131,132,133,134), FieldType.AKTION),
        new Field(130, 0.735, 0.4, Arrays.asList(131,132,133,134), FieldType.TIER),
        new Field(131, 0.703, 0.428, Arrays.asList(132,133,134), FieldType.AKTION),
        new Field(132, 0.668, 0.454, Arrays.asList(133,134), FieldType.ZAHLTAG),
        new Field(133, 0.668, 0.504, Arrays.asList(134), FieldType.AKTION),
        new Field(134, 0.665, 0.565, Arrays.asList(134), FieldType.RUHESTAND)
    );

    @Override
    public List<Field> getBoard() {
        return Collections.unmodifiableList(BOARD);
    }

    @Override
    public Field getFieldByIndex(int index) {
        for (Field field : BOARD) {
            if (field.getIndex() == index) {
                return field;
            }
        }
        return null;
    }
    
    /**
     * Statische Methode für Abwärtskompatibilität, sollte in neuen Code nicht verwendet werden.
     * Verwenden Sie stattdessen die Instanzmethode.
     *
     * @return Eine unveränderbare Liste aller Felder     * @deprecated Verwende die Instanz über Dependency Injection
     * @since 1.0
     */
    @Deprecated(since = "1.0", forRemoval = false)
    public static List<Field> getBoardStatic() {
        return Collections.unmodifiableList(BOARD);
    }
    
    /**
     * Statische Methode für Abwärtskompatibilität, sollte in neuen Code nicht verwendet werden.
     * Verwenden Sie stattdessen die Instanzmethode.
     *     * @param index Der Index des gesuchten Felds
     * @return Das gefundene Feld oder null, wenn kein Feld mit diesem Index existiert
     * @deprecated Verwende die Instanz über Dependency Injection
     * @since 1.0
     */
    @Deprecated(since = "1.0", forRemoval = false)
    public static Field getFieldByIndexStatic(int index) {
        for (Field field : BOARD) {
            if (field.getIndex() == index) {
                return field;
            }
        }
        return null;
    }
}
