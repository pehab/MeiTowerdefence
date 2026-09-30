# Balancing-Review 0.3.0

Stand: 30.09.2026, nach dem Tausch Kreuzung/Serpentinen und der neuen Festung-Pause.
Dies ist eine Beurteilung anhand der tatsächlichen Level-, Turm- und Gegnerdaten,
keine Aussage über gemessene Gewinnquoten. Platzierung, Upgrades, Sternverteilung,
Verlangsamung und manuelle Frühstarts können das Ergebnis stark verändern.

## Kampagne

Ohne Sternboni: Weglänge in Rasterzellen; HP summiert alle Gegner inklusive Bossen
und Wellenmultiplikatoren. Killgold setzt voraus, dass alle Gegner besiegt werden;
es enthält weder Goldader noch Frühstartboni. Gesamt-HP allein misst keine Schwierigkeit.

| Reihenfolge | Level | Wellen | Startgold | Bodenweg | Gegner | Gesamt-HP, gerundet | Killgold | Pragmatische Bewertung |
|---|---|---:|---:|---:|---:|---:|---:|---|
| 1 | Waldpfad | 6 | 220 | 22 | 52 | 1.870 | 274 | Leichter Einstieg ist passend: keine Panzer, Flieger oder Bosse. |
| 2 | Bergpass | 8 | 180 | 28 | 62 | 3.530 | 414 | Sinnvolle erste Panzerprüfung. Der längere Weg hilft, das geringere Startgold verlangt Planung. |
| 3 | Talkessel | 9 | 260 | 21 | 58 | 4.180 | 540 | Erster größerer Lernsprung: kurze Bodenroute, Flieger ab Welle 4 und erster Boss. Hohes Startgold federt ihn ab. |
| 4 | Flussufer | 7 | 240 | 24 | 86 | 5.241 | 716 | Spürbarer Sprung durch gemischte Wellen und Flieger bereits in Welle 2. Die obere Bodenroute erlaubt gemeinsame Abdeckung. |
| 5 | Kreuzung | 8 | 225 | 33 | 137 | 9.233 | 1.062 | Stärkere Wellen, aber sehr günstige Mehrfachabdeckung. Vor Serpentinen plausibel. |
| 6 | Serpentinen | 8 | 210 | 38 | 107 | 7.764 | 882 | Langer Bodenweg, aber stark eingeschränkte Luftabwehr. Schwierigkeit entsteht aus Platzierung statt bloßer Gegnermenge. |
| 7 | Festung | 9 | 190 | 25 | 162 | 15.666 | 1.480 | Weiterhin harter Einstieg; die neue Pause beseitigt automatisches Aufstauen, schwächt aber einzelne Wellen nicht. |
| 8 | Letzter Wall | 12 | 230 | 26 | 304 | 29.236 | 2.622 | Klarer Endkampf, möglicherweise weiterhin zu abrupt: deutlich mehr Gegner und HP, wieder automatische Überlappung. |

## Wo die größten Risiken bleiben

- **Talkessel:** Flugroute bei y=1, Bodenroute frühestens y=3. Nicht jeder gute
  Bodenturm erreicht Flieger. Welle 4 verlangt rechtzeitig vorbereitete Luftabwehr;
  der Boss sollte mit höherem Einzelschaden und Eis bekämpft werden.
- **Flussufer:** Luftabwehr schon in Welle 2 ist der frühe Engpass. Nicht sämtliche
  Einnahmen in reine Bodenabwehr stecken. Zunächst im Spiel beobachten, keine
  pauschale Abschwächung aus den Daten allein ableiten.
- **Kreuzung/Serpentinen:** Für ungespezialisierte Bogenschützen mit Reichweite 3,2
  gibt es 30 gegenüber nur 7 legalen Bauzellen, die die Flugroute überhaupt erreichen.
  Das zählt mögliche Standorte, nicht gleichzeitig mögliche Treffer oder DPS.
  Der Leveltausch passt deshalb trotz höherer Gesamt-HP in Kreuzung. Serpentinen
  sollte weiterhin auf mehreren Luftabwehr-Standorten getestet werden.
- **Festung:** Welle 1 besteht bereits aus acht Panzern mit jeweils 143 HP.
  Basisbogenschützen verursachen gegen Rüstung 5 nur 3 Schaden pro Treffer.
  Ohne Sternbonus reichen 190 Startgold nicht für einen Bogenschützen auf Stufe 4
  (205 Gold einschließlich Spezialisierung). Kanone und Eis beziehungsweise
  hochgerüstete Bogenschützen sind naheliegende Alternativen. Falls weiterhin
  regelmäßig Welle 1/2 scheitert, zuerst Startgold 190→220 oder Panzerzahl 8→6
  einzeln ausprobieren. Wenn erst spätere Wellen scheitern, ist das kein Beleg
  dafür, dass die Eröffnungswelle leichter werden muss.
- **Letzter Wall:** Abstände bleiben vier Sekunden nach dem letzten Spawn; anders
  als Festung wartet dieses Level nicht auf das freie Feld. Das ist nun ein
  zusätzlicher Regel- und Schwierigkeitssprung. Falls derselbe Überroll-Effekt
  auftritt, zuerst die Festung-Warteregel übernehmen und die sechs Sekunden
  Baupause testen, bevor HP oder Gegnerzahl reduziert werden.

## Turmmix und Sterne

Dauerfeuer ist nicht grundsätzlich schwach gegen Panzer: Auf Stufe 4 hat es ohne
Sternbonus 17,2 Schaden pro Treffer, gegen Panzer effektiv 12,2. Das ergibt nominal
41,5 DPS auf ein dauerhaft erreichbares Einzelziel. Der Scharfschütze derselben
Stufe kommt dank Rüstungsdurchdringung auf 43,3 DPS. Gegen Bosse beträgt der
Unterschied bereits 24,5 gegenüber 37,7 DPS. Der zufällige zweite Dauerfeuer-Treffer
hilft gegen Gruppen, erhöht aber nicht den Schaden am einzigen Ziel. Reisezeit,
Zielwechsel und verschwendete Projektile sind in diesen Idealwerten nicht enthalten.
Spezialisierungen werden beim Upgrade von Stufe 3 auf 4 gewählt.

Die Schwäche gegenüber Panzern betrifft vor allem niedrigstufige Bogenschützen.
Praktisch: Dauerfeuer für Luft und Gruppen behalten, dazu Einzelschaden gegen Bosse,
Kanonen gegen Bodengruppen und Eis für mehr Beschusszeit. Startkapital und
Geschärfte Pfeile helfen besonders beim Festung-Einstieg. Stern-Upgrades können
kostenlos mit vollständiger Rückerstattung neu verteilt werden.

Goldader sammelt seinen Bonus jetzt in exakten Hundertsteln über Abschüsse innerhalb
einer Partie. Damit bringen auch kleine Belohnungen langfristig tatsächlich +8 %
pro Stufe; das frühere Abrunden pro Gegner ist behoben. Startkapital hilft sofort beim
Einstieg, Goldader wächst mit den im Level erzielten Abschüssen.

Zusätzlich sind die Turmrollen geschärft: Eis verteilt Kontrolleffekte und bevorzugt
ungefrorene Gegner, Feuer bevorzugt nicht brennende Ziele, Kanonen wählen Bodengruppen
im tatsächlichen Explosionsradius, Bogenschützen bevorzugen Flieger. Bei Gleichstand
zählt die verbleibende Laufzeit zur Basis bei normaler Geschwindigkeit. Feuer- und
Eistreffer umgehen Rüstung vollständig; Brandschaden umgeht sie weiterhin. Dadurch
sind Feuer/Eis auch gegen Panzer effektiver. Kanonen/Feuer treffen mit Bodensplash
keine Flieger mehr. Die geänderten Regeln müssen im nächsten Balancing-Spieltest
mit derselben Sternverteilung berücksichtigt werden.

Die Kampagne zahlt maximal 24 Sterne; alle Shop-Upgrades zusammen kosten 177.
Kumulative Erfolge schaffen mit 42 Meilensteinen und insgesamt 198 möglichen Sternen
nun eine zusätzliche Fortschrittsquelle bereits während der Kampagne. Endlos wird
weiterhin erst nach Abschluss aller acht Kampagnenlevel freigeschaltet; Erfolge können
hingegen schon beim ersten Festung-Durchlauf helfen. Die große Sternmenge erfordert
langfristig steigende Abschussziele und unterschiedliche Turmtypen.

## Unendlich

Der Einstieg ist mit 240 Gold und zentraler Luft-/Bodenabdeckung moderat. Ab Welle 5
kommen zusätzliche Flieger, ab Welle 8 zusätzliche schnelle Gegner in geraden Wellen,
jede zehnte Welle hat einen Boss und Begleitung. HP und Gegnermenge wachsen zugleich;
mit festem Turmlimit Stufe 5 wird der Modus irgendwann zwangsläufig schwieriger.
Die vier Sekunden Pause können Wellen überlagern. Aus den Daten lässt sich keine
verlässliche erreichbare Bestwelle oder faire Grenze ableiten.

Für jeden neuen Rekord von zehn vollständig überstandenen Wellen gibt es einmalig
einen ausgebbaren Stern. Belohnungen werden bei 10, 20, 30 usw. gespeichert; bereits
belohnte Meilensteine geben auch in neuen Läufen keine weiteren Rekordsterne.
Abschüsse in weiteren Läufen treiben stattdessen die kumulativen Erfolgsreihen voran. Gestartete oder vollständig
erschienene Gegner genügen nicht: Alle Gegner dieser und früherer Wellen müssen
besiegt oder durchgelaufen sein, während die Basis überlebt. Die tödliche Welle
zählt nicht. Bestehende Bestwerte werden nicht rückwirkend belohnt.

## Nächster Spieltest

Festung zunächst mit neuer Pause und bisherigem Sternstand spielen; notieren,
in welcher Welle die ersten größeren Verluste auftreten und ob Boden oder Luft
durchbrechen. Anschließend Letzter Wall auf automatisches Aufstauen prüfen.
Kreuzung/Serpentinen mit gleicher Sternverteilung vergleichen. Pro Versuch nur
eine Stellschraube ändern; CI prüft Regeln und Regressionen, keine gefühlte Schwierigkeit.
