Panda GDPR-validator
=====================================

Panda GDPR-validator inneholder kode som sjekker om Panda-prosjektet etterlever GDPR.

Per dags dato inneholder det kun et CLI-verktøy som sjekker om det eksisterer fødselsnummere i filene i en folder,
eller en enkeltfil, og hvorvidt de er gyldige eller ikke.


Hvordan bygge Panda GDPR-validator
====================================

I rotmappen av prosjektet, kjør ```mvn clean install```.


Hvordan testkjøre Panda GDPR-validator
========================================

Hent `panda-gdpr-validator-cli-*-jar-with-dependencies.jar*` fra *panda-gdpr-validator/panda-gdpr-validator-cli/target*. Deretter
kjører man:

```sh
java -jar panda-gdpr-validator-cli-*-jar-with-dependencies.jar [-hV] -m=<modus> [-t=<filtyper>]... <bane>
```

Eksempel:

```sh
java -jar panda-gdpr-validator-cli-*-jar-with-dependencies.jar -m fødselsnummer -t feature -t md -t java panda-fakturering/ > fnr-pf.txt
```

Parametere er:

- **m: modus**. Det eksisterer kun en modus for øyeblikket: fødselsnummer.
- **t: filtype**. En liste av filtyper å sjekke data i. Angi som liste på denne måten: `-t filtype1 -t filtype2`.
