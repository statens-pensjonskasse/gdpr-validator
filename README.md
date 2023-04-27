GDPR-validator
=====================================

GDPR-validator inneholder kode som sjekker om prosjekter etterlever GDPR.

Per dags dato inneholder det kun et CLI-verktøy som sjekker om det eksisterer fødselsnummere i filene i en folder,
en enkeltfil, et repository eller alle repositories i et prosjekt, og hvorvidt de er gyldige eller ikke.

Det ble opprinnelig laget for å sjekke om Panda-prosjektet inneholder gyldige fødselsnummere i koden sin, men er
åpent for å bli brukt av alle.

Hvordan bygge GDPR-validator
====================================

I rotmappen av prosjektet, kjør `mvn clean install`.

For å bygge et GraalVM native-image, kjør `mvn clean install -Dnative`. For at dette skal fungere må man ha GraalVM
installert, samt native-image. Man får da en native binærfil.


Hvordan kjøre GDPR-validator
========================================

Hent `gdpr-validator-cli-*-jar-with-dependencies.jar*` fra *gdpr-validator/gdpr-validator-cli/target*.
Eventuelt kan man hente `gdprvalidator` fra samme mappe, hvis man har bygget et native-image.

Deretter kjører man:

```sh
java -jar gdpr-validator-cli-*-jar-with-dependencies.jar [-bghnoV] [-f=<fnrtype>] -m=<modus> [-t=<filtyper>]... <bane>
```

Eksempel lokalt filsystem med ordinære fødselsnummere:

```sh
java -jar gdpr-validator-cli-*-jar-with-dependencies.jar \
  -m fødselsnummer \
  -f ordinær \
  -t feature -t md -t java -t csv -t xml -t sh -t txt -t sql -t r -t js -t ts -t html -t css \
  --visGyldighet --visNestenGyldighet --visOppsummering \
  panda-fakturering/ > fnr.txt
```

Eksempel enkelt Git-repository med kasper-fødselsnummere:

```sh
java -jar gdpr-validator-cli-*-jar-with-dependencies.jar \
  -m fødselsnummer_ett_repo \
  -f kasper \
  -t feature -t md -t java -t csv -t xml -t sh -t txt -t sql -t r -t js -t ts -t html -t css \
  --visOppsummering --visGyldighet --visOppsummering --visFilbane \
  https://git.spk.no/scm/pnd/panda-fakturering-aggregering.git > fnr.txt
```

Eksempel alle Git-repositories i et prosjekt med kasper-fødselsnummere (separert med semikolon mellom dato og resten):

```sh
java -jar gdpr-validator-cli-*-jar-with-dependencies.jar \
  -m fødselsnummer_alle_repoer \
  -f kasper_med_semikolon \
  -t feature -t md -t java -t csv -t xml -t sh -t txt -t sql -t r -t js -t ts -t html -t css \
  --visGyldighet --visNestenGyldighet --visOppsummering \
  PND > fnr.txt
```

Parametere er:

- **m: modus**. Moduser: fødselsnummer, fødselsnummer_ett_repo, fødselsnummer_alle_repoer.
    * Modusen fødselsnummer sjekker det lokale filsystemet.
    * Modusen fødselsnummer_ett_repo sjekker et Git-repository. Repositoryet blir lastet ned.
    * Modusen fødselsnummer_alle_repoer sjekket alle Git-repositoryer i ett prosjekt (f.eks. PND eller PER). Alle repositoryene blir lastet ned.
- **f: fnrtype**. Fødselsnummertype. Enten ordinær (ddMMyyiiiss), kasper (yyyyMMddiiiss) eller kasper_med_semikolon (yyyyMMdd;iiiss).
- **t: filtype**. En liste av filtyper å sjekke data i. Angi som liste på denne måten: `-t filtype1 -t filtype2`.
- **o: visOppsummering**. Vis oppsummering av resultatene.
- **g: visGyldighet**. Vis gyldighet av fødselsnummer.
- **n: visNestenGyldighet**. Vis nesten gyldighet av fødselsnummer.
- **b: visFilbane**. Vis filbanen fødselsnummeret eksisterer i.
