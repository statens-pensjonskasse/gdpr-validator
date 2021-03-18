GDPR-validator
=====================================

GDPR-validator inneholder kode som sjekker om prosjekter etterlever GDPR.

Per dags dato inneholder det kun et CLI-verktøy som sjekker om det eksisterer fødselsnummere i filene i en folder,
eller en enkeltfil, og hvorvidt de er gyldige eller ikke.


Hvordan bygge GDPR-validator
====================================

I rotmappen av prosjektet, kjør ```mvn clean install```.


Hvordan kjøre GDPR-validator
========================================

Hent `panda-gdpr-validator-cli-*-jar-with-dependencies.jar*` fra *panda-gdpr-validator/panda-gdpr-validator-cli/target*. Deretter
kjører man:

```sh
java -jar panda-gdpr-validator-cli-*-jar-with-dependencies.jar [-hV] [-f=<fnrtype>] -m=<modus> [-t=<filtyper>]... <bane>
```

Eksempel lokalt filsystem med ordinære fødselsnummere:

```sh
java -jar panda-gdpr-validator-cli-*-jar-with-dependencies.jar \
  -m fødselsnummer \
  -f ordinær \
  -t feature -t md -t java -t csv -t xml -t sh -t txt -t sql -t r -t js -t ts -t html -t css \
  panda-fakturering/ > fnr.txt
```

Eksempel enkelt Git-repository med kasper-fødselsnummere:

```sh
java -jar panda-gdpr-validator-cli-*-jar-with-dependencies.jar \
  -m fødselsnummer_ett_repo \
  -f kasper \
  -t feature -t md -t java -t csv -t xml -t sh -t txt -t sql -t r -t js -t ts -t html -t css \
  http://git.spk.no/scm/pnd/panda-fakturering-aggregering.git > fnr.txt
```

Eksempel alle Git-repositories i et prosjekt med kasper-fødselsnummere (separert med semikolon mellom dato og resten):

```sh
java -jar panda-gdpr-validator-cli-*-jar-with-dependencies.jar \
  -m fødselsnummer_alle_repoer \
  -f kasper_med_semikolon \
  -t feature -t md -t java -t csv -t xml -t sh -t txt -t sql -t r -t js -t ts -t html -t css \
  PND > fnr.txt
```

Parametere er:

- **m: modus**. Moduser: fødselsnummer, fødselsnummer_ett_repo, fødselsnummer_alle_repoer.
    * Modusen fødselsnummer sjekker det lokale filsystemet.
    * Modusen fødselsnummer_ett_repo sjekker et Git-repository. Repositoryet blir lastet ned.
    * Modusen fødselsnummer_alle_repoer sjekket alle Git-repositoryer i ett prosjekt (f.eks. PND eller PER). Alle repositoryene blir lastet ned.
- **f: fnrtype**. Fødselsnummertype. Enten ordinær (ddMMyyiiiss), kasper (yyyyMMddiiiss) eller kasper_med_semikolon (yyyyMMdd;iiiss).
- **t: filtype**. En liste av filtyper å sjekke data i. Angi som liste på denne måten: `-t filtype1 -t filtype2`.

