/*
 * Copyright 2024 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.butterfly.bcc.helper;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Patrik Karlström
 */
public class Ntc3k {

    private static Properties sProperties;

    public static Double convertToCelcius(Double value) {
        if (value == null) {
            return null;
        }
        if (sProperties == null) {
            sProperties = new Properties();
            try {
                sProperties.load(new StringReader(TABLE));
            } catch (IOException ex) {
                Logger.getLogger(Ntc3k.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        var celcius = -273.15;
        var minDiff = Integer.MAX_VALUE;

        for (var entry : sProperties.entrySet()) {
            var resistance = Integer.parseInt((String) entry.getKey());
            var diff = Math.abs(value - resistance);
            if (diff < minDiff) {
                minDiff = (int) diff;
                celcius = Double.parseDouble((String) entry.getValue());
            }
        }

        return celcius;
    }

    public static final String TABLE = """
                                     96941=-39.44
                                     90108=-38.33
                                     83804=-37.22
                                     77983=-36.11
                                     72607=-35
                                     67637=-33.89
                                     63041=-32.78
                                     58789=-31.67
                                     54851=-30.56
                                     51173=-29.44
                                     47795=-28.33
                                     44663=-27.22
                                     41756=-26.11
                                     39059=-25
                                     36553=-23.89
                                     34225=-22.78
                                     32061=-21.67
                                     30047=-20.56
                                     28157=-19.44
                                     26414=-18.33
                                     24790=-17.22
                                     23277=-16.11
                                     21865=-15
                                     20549=-13.89
                                     19320=-12.78
                                     18173=-11.67
                                     17101=-10.56
                                     16091=-9.44
                                     15155=-8.33
                                     14280=-7.22
                                     13461=-6.11
                                     12694=-5
                                     11975=-3.89
                                     11302=-2.78
                                     10671=-1.67
                                     10079=-0.56
                                     9519=0.56
                                     8999=1.67
                                     8510=2.78
                                     8050=3.89
                                     7619=5
                                     7213=6.11
                                     6831=7.22
                                     6472=8.33
                                     6134=9.44
                                     5813=10.56
                                     5513=11.67
                                     5231=12.78
                                     4965=13.89
                                     4714=15
                                     4478=16.11
                                     4254=17.22
                                     4043=18.33
                                     3844=19.44
                                     3655=20.56
                                     3477=21.67
                                     3309=22.78
                                     3150=23.89
                                     3000=25
                                     2858=26.11
                                     2723=27.22
                                     2596=28.33
                                     2475=29.44
                                     2360=30.56
                                     2252=31.67
                                     2149=32.78
                                     2051=33.89
                                     1959=35
                                     1871=36.11
                                     1788=37.22
                                     1709=38.33
                                     1634=39.44
                                     1562=40.56
                                     1494=41.67
                                     1430=42.78
                                     1368=43.89
                                     1310=45
                                     1255=46.11
                                     1202=47.22
                                     1151=48.33
                                     1104=49.44
                                     1058=50.56
                                     1014=51.67
                                     973=52.78
                                     933=53.89
                                     895=55
                                     860=56.11
                                     825=57.22
                                     793=58.33
                                     761=59.44
                                     731=60.56
                                     703=61.67
                                     676=62.78
                                     650=63.89
                                     625=65
                                     601=66.11
                                     578=67.22
                                     556=68.33
                                     536=69.44
                                     516=70.56
                                     496=71.67
                                     478=72.78
                                     461=73.89
                                     444=75
                                     428=76.11
                                     413=77.22
                                     398=78.33
                                     384=79.44
                                     370=80.56
                                     357=81.67
                                     345=82.78
                                     333=83.89
                                     321=85
                                     310=86.11
                                   """;
}
