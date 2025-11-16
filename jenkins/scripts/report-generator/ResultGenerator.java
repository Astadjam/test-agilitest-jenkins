import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.regex.Pattern;

class ResultGenerator{
    public static void main(String[] args){
        // Récupération des arguments et check rapide 

        if(args.length < 3){
            System.exit(1);
            return;
        }

        var templateFilePath = args[0];
        var atsScriptsFilePath = args[1];
        var resultFilePath = args[2];

        // Parsing du fichier scripts

        var suitesResultsMap = new HashMap<String, SuiteResult>(); 

        try {
            var atsScriptsContent = String.join("",Files.readAllLines(Path.of(atsScriptsFilePath)));
            var regex = "\\{\\\"name\\\":\\\"([^\\\"]*)\\\",\\\"suite\\\":\\\"([^\\\"]*)\\\",\\\"duration\\\":[^,]*,\\\"passed\\\":([^\\,]*),\\\"actions\\\":[^\\,]*,\\\"time\\\":([^\\}]*)\\}";
            var compiledRegex = Pattern.compile(regex);
            var regexMatcher = compiledRegex.matcher(atsScriptsContent);
            var succeedTests = 0;
            var failedTests = 0;
            BigInteger firstExecutionTime = null;

            while(regexMatcher.find()) {
                // Récupération des infos de succès

                var scriptName = regexMatcher.group(1);
                var suiteName = regexMatcher.group(2);
                var passed = regexMatcher.group(3).equals("true");
                var time = BigInteger.valueOf(Long.parseLong(regexMatcher.group(4)));

                // Récupération des résultats de la suite parente

                if(!suitesResultsMap.containsKey(suiteName)){
                    suitesResultsMap.put(suiteName, new SuiteResult(true, new HashMap<String, Boolean>()));
                }


                // Ajout du résultat du script dans la suite et incrémentation des variables

                var suiteResult = (SuiteResult) suitesResultsMap.get(suiteName);

                suiteResult.scriptsResult.put(scriptName, passed);

                if(!passed) {
                    suiteResult.success = false;
                    failedTests++;
                }
                else{
                    succeedTests++;
                }

                if(firstExecutionTime == null){
                    firstExecutionTime = time;
                }
            }

            // Création du fichier résultat et remplacement des modèles

            var tables = "";

            for(var suiteName : suitesResultsMap.keySet()){
                var suitesValue = suitesResultsMap.get(suiteName);

                tables += "<table\r\n" + //
                    "        style=\"\r\n" + //
                    "            font-family: Arial;\r\n" + //
                    "            font-size: 18px;\r\n" + //
                    "            border-collapse: collapse;\r\n" + //
                    "            color: #000064;\r\n" + //
                    "            margin-top: 20px;\r\n" + //
                    "        \"\r\n" + //
                    "    >\r\n" + //
                    "        <thead\r\n" + //
                    "            style=\"\r\n" + //
                    "                background-color: #e5e5ef;\r\n" + //
                    "            \"\r\n" + //
                    "        >\r\n" + //
                    "            <tr>\r\n" + //
                    "                <th\r\n" + //
                    "                    style=\"\r\n" + //
                    "                        width: 700px;\r\n" + //
                    "                        font-weight: bold;\r\n" + //
                    "                        text-transform: uppercase;\r\n" + //
                    "                        text-align: left;\r\n" + //
                    "                        padding: 15px;\r\n" + //
                    "                        border-top-left-radius: 10px;\r\n" + //
                    "                        border-bottom-left-radius: 10px;\r\n" + //
                    "                    \"\r\n" + //
                    "                >" + suiteName + "</th>\r\n" + //
                    "\r\n" + //
                    "                <th\r\n" + //
                    "                    style=\"\r\n" + //
                    "                        padding: 15px;\r\n" + //
                    "                        border-top-right-radius: 10px;\r\n" + //
                    "                        border-bottom-right-radius: 10px;\r\n" + //
                    "                        font-size: 16px;\r\n" + //
                    "                        color: " + (suitesValue.success ? "green" : "red") + ";\r\n" + //
                    "                        width: 300px;\r\n" + //
                    "                        text-align: right;\r\n" + //
                    "                    \"\r\n" + //
                    "                >" + (suitesValue.success ? "OK" : "KO") +"</th>\r\n" + //
                    "            </tr>\r\n" + //
                    "        </thead>\r\n" + //
                    "       <tbody>\r\n";

                for(var scriptName : suitesValue.scriptsResult.keySet()){
                    var scriptValue = suitesValue.scriptsResult.get(scriptName);

                    tables += "<tr>\r\n" + //
                                                "                <td\r\n" + //
                                                "                    style=\"\r\n" + //
                                                "                        width: 700px;\r\n" + //
                                                "                        font-weight: bold;\r\n" + //
                                                "                        text-align: left;\r\n" + //
                                                "                        padding: 15px;\r\n" + //
                                                "                    \"\r\n" + //
                                                "                >" + scriptName + "</td>\r\n" + //
                                                "                \r\n" + //
                                                "                <td\r\n" + //
                                                "                    style=\"\r\n" + //
                                                "                        padding: 15px;\r\n" + //
                                                "                        border-top-right-radius: 10px;\r\n" + //
                                                "                        border-bottom-right-radius: 10px;\r\n" + //
                                                "                        font-size: 16px;\r\n" + //
                                                "                        color: " + (scriptValue ? "green" : "red") +";\r\n" + //
                                                "                        width: 300px;\r\n" + //
                                                "                        text-align: right;\r\n" + //
                                                "                    \"\r\n" + //
                                                "                >" + (scriptValue ? "OK" : "KO") + "</td>\r\n" + //
                                                "            </tr>";
                }

                tables += "</tbody>\r\n" + //
                    "    </table>";
            }

            var templateFileContent = String.join("\n",Files.readAllLines(Path.of(templateFilePath)));
            var totalTests = succeedTests + failedTests;
            var dateFormatter = DateTimeFormatter.ofPattern("dd/M/y HH:mm");

            long timestamp = firstExecutionTime.longValue();
            Instant instant = Instant.ofEpochMilli(timestamp);
            LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

            templateFileContent = templateFileContent
                .replaceAll("\\{executionDate\\}", dateFormatter.format(date))
                .replaceAll("\\{countOfExecutedTests\\}", String.valueOf(totalTests))
                .replaceAll("\\{countOfPassedTests\\}", String.valueOf(succeedTests))
                .replaceAll("\\{countOfFailedTests\\}", String.valueOf(failedTests))
                .replaceAll("\\{percentage\\}", String.valueOf(((float)succeedTests / (float)totalTests) * 100.0).substring(0,5) + " ")
                .replaceAll("\\{tables\\}", tables);

            Files.writeString(Path.of(resultFilePath),templateFileContent);
            
            System.exit(0);
        }
        catch(Throwable e){
            System.err.println(e);
            System.exit(1);
        }
    }


    // Résultat d'exécution d'une suite
    public static class SuiteResult{
        Boolean success;
        HashMap<String, Boolean> scriptsResult; 
        
        SuiteResult(Boolean success,HashMap<String, Boolean> scriptsResult){
            this.success = success;
            this.scriptsResult = scriptsResult;
        }
    }
}