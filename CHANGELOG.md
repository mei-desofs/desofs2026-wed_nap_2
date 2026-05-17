# Changelog

## [1.0.6](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.0.5...v1.0.6) (2026-05-17)


### Features

* Development of Invoice generation / download and Directory structure / invoice storage [#6](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/6) [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([31a84cd](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/31a84cd9de6ecabfdd85eb12b7b912f588043368))
* Enable TLS Protocol, Cache Control and Improvement of the cryptography of the activation keys and key management ([fa66b64](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/fa66b64e2aa55cb47ade91da0c34f84409216d92))
* Testing of the implementation on the application to guarantee the 99% availability [#6](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/6) [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([0cdaa68](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/0cdaa6806d5f6d46daf2760b7330fbaa8328286c))


### Bug Fixes

* Changin docker compose to put the API work again [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([5744a20](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/5744a20bad1fc2c3c8484d8c99acc587b5f2c5fb))
* Changing the startup time for the docker-compose to verify the healthy of it [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([c568e6c](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/c568e6c38e762bb0789431735cb15d825200a5b7))
* SFTP Trusted Host Key Verification and TLS Protocol + Cipher Suites [#6](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/6) [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([2fe6b7a](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/2fe6b7adef27dcfa46494c6e51e995922538946e))

## [1.0.5](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.0.4...v1.0.5) (2026-05-17)


### Bug Fixes

* DAST scan endpoint. Merged docker build and DAST workflows into a workflow with 3 jobs ([b19546d](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/b19546d6cbb86c07ce0da54f7ceb0acf5440d4ce))
* environment variables in the docker build, scan and DAST workflow [#5](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/5) ([77219e1](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/77219e174d80d1b67adb04469b6a688c75ee9aec))
* Increase health check waiting time for github actions [#5](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/5) ([2d5dc60](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/2d5dc60182ca4e7b277be9936f45dd57f9e75a8c))
* keycloak health check step [#5](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/5) ([59ea8ca](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/59ea8ca04a623f989fd30087ba271c051aadabae))
* Merge build, scan and DAST jobs [#5](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/5) ([0889e76](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/0889e76f5c15be813bbaccbe0d746556c10c451e))
* Merge build, scan and DAST jobs [#5](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/5) ([6d6730f](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/6d6730f7f46b58d4ff37790aa383ac6d14b610d1))

## [1.0.4](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.0.3...v1.0.4) (2026-05-17)


### Features

* Configuration of the HSTS, CORS allowlist, X-Content-Type-Options and Output encoding in files. Implementations of tests for these functionalities [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) ([c46dea0](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/c46dea0780fd9d774042eb198d4df5af78407455))
* Implementation of Context-specific password blocklist, common password list check and HIBP breached password check [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) ([fafedff](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/fafedffb19c47d48a2c83b24dca1e5dd69dcae0f))


### Bug Fixes

* Correction of the SHA-1 of PasswordPolicyService [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) ([8659723](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/8659723389794c96534cd71e12269c8538817d92))

## [1.0.3](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.0.2...v1.0.3) (2026-05-16)


### Features

* Development of the Category and Metrics for the games [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#11](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/11) ([0816dd4](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/0816dd4abe9d042ed1584cbf05d744f84dd60e44))


### Bug Fixes

* Correction of Unit Tests of Domain [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) ([9839491](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/98394918b7926f876b8530decfe9247219522220))

## [1.0.2](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.0.1...v1.0.2) (2026-05-16)


### Bug Fixes

* docker-compose.yml app name [#4](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/4) ([5c96214](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/5c962147ea500ab83ceb55fd9d9e977c4a121130))
* remove context loading test ([5135f30](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/5135f305417585684928a4d60246c3cb786ea152))
* replace secrets as vars in workflows [#4](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/4) ([7e2ad86](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/7e2ad863c93178db44964dd77150b7c44c2d2f8b))

## [1.0.1](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.0.0...v1.0.1) (2026-05-16)


### Bug Fixes

* Correction of docker-compose to run the keycloak with the SFTP server working ([c3661fd](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/c3661fdda94583c1dba6aed6f5fb83f75c66aa39))
* Modification of the JSON import for Postman and the realm of keycloak ([7e4736a](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/7e4736afe40c2f4b834e17f0820422f1fe850e3c))

## 1.0.0 (2026-05-16)


### Features

* Creation of the API Domain Objects Base with SpringBoot ([c519a7e](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/c519a7e59482d729f9a59c6f5ba34b74c23a8e73))
* Docker implementation ([68ad0ed](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/68ad0ed2699b7d02bdf95ae3ddb0e6b568f1fafd))
* Starting of the implementation of Keycloak ([0048adc](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/0048adc6b68b39ca1ac2e744cc670f7b452d0ff6))


### Bug Fixes

* Configuration of database ([57fec9c](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/57fec9cdece81f2c2e73472cc5ed7d78ac9ccdb8))
* Correction of the implementation with keycloak ([9532e36](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/9532e36e3099a0f59b602ffef01ba4aac6395dea))
* false positives in GitLeaks secret scanning. Fix DockerHub authentication ([3d44289](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/3d442895f89d9462f9e3f3023cd4f43c1e8f1608))
* fixed DAST report upload path ([f081035](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/f081035c7a6d9af9de562853002328dfc7446118))
* fixed dast workflow heatlh endpoint ([c2f0b71](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/c2f0b71d57c14dfd61fa28cd1b47d9ef306f4e1a))
* fixed invalid report path in dast workflow ([23b34a1](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/23b34a1f1ad5c313fb207a79dcc08ca76c6ce40b))
* remove DockerScout step from docker-build.yml workflow ([fbd6c33](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/fbd6c33087fd8d2df436a1f2ddfd0f87e41fc3cb))
* replace secrets inside docker-compose with environment variables ([7316d17](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/7316d178d32a0e7483c5e4430e7fc4a036cc3c96))
* Trivy report path when uploading it as an artifact to github ([a020ad7](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/a020ad7cd2f19ea65608aef5660a32b28d7209ec))
