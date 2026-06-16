# Changelog

## [4.1.0](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v4.0.0...v4.1.0) (2026-06-16)


### Features

* Implementation of validateSubDir and validateRemotePath on SFTP, implementation of SSRF: enforce egress allowlist on all outbound RestTemplate calls [#4](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/4) [#6](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/6) [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([237c637](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/237c637409799e2f8a33f10e4a804eb7c3d6a753))

## [4.0.0](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v3.1.0...v4.0.0) (2026-06-16)


### ⚠ BREAKING CHANGES

* RAWG API integration, Keycloak session management and DB schema fixes #9 #10 #12 #13 #16

### Features

* Implementation of deactivate all user sessions and deactivate user account [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#10](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/10) ([ea86898](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/ea8689880ca9a107aa9fa79cd8de2fa137b493fe))
* RAWG API integration, Keycloak session management and DB schema fixes [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#10](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/10) [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) [#16](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/16) ([92af8e7](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/92af8e727b581c5c9a7cf89b9ba7534fa0900ed0))

## [3.1.0](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v3.0.0...v3.1.0) (2026-06-16)


### Features

* implement ASVS V9.2.2/V9.2.3/V9.2.4 - JWT typ and audience claim validation [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) ([a8acb73](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/a8acb7317e758509eea5d8c0793114f4ef6b06d3))


### Bug Fixes

* Correction of the JwtDecoder [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) ([e0ead0e](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/e0ead0ee542ce46634fd30cfff5d3adc5120af86))

## [3.0.0](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v2.0.1...v3.0.0) (2026-06-15)


### ⚠ BREAKING CHANGES

* port 8080 is no longer published; generate TLS certs before starting the stack (./nginx/generate-self-signed-cert.sh or .ps1)

### Features

* Implement ASVS V12 secure communication via nginx TLS reverse proxy ([e8d476f](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/e8d476f581c9b3297ec76ac6f079ca3d9095a522))
* Implementation of the Security Vulnerability & Architecture Risk Policy on the application [#3](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/3) [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([fcb921c](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/fcb921ce8d935910d58c089af553f97fa9a17403))
* Implementation of the Security Vulnerability & Architecture Risk Policy on the application [#3](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/3) [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([9744cc9](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/9744cc960ea253ff139d67115b51beef51041243))
* Implementation of the Security Vulnerability & Architecture Risk Policy on the application [#3](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/3) [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([52c81e3](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/52c81e3250abd56da79cfa4be9e535429d33018b))
* Improvement the API with the ASVS Rules specific about Authentication, Logs, RateLimit and Security Audit [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#10](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/10) [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) [#16](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/16) ([a3f1a09](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/a3f1a09eca18be882f56e58c7d4fbbacc36ad220))
* Improvement the API with the ASVS Rules specific about Rate Limit, Log Events, Http Trace, CRLF Injection prevention [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#10](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/10) [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) [#16](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/16) ([1fbb0f9](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/1fbb0f9c23e0626cff010bb39c57c9970a7a2679))
* Integration of OAuthGrantConfigTest and improvement of SecurityAuditService and correction of testes [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) ([483f5f6](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/483f5f69b8c91b8247625e84449c60f1d8a8a930))


### Bug Fixes

* Correction of the SecurityIntegrationTests changing isForbidden to isBadRequest [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) [#13](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/13) ([83cde9a](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/83cde9a984c282e3fc3a7b9461afe39b2d45471d))
* GameRepository correction on cast strings ([65af744](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/65af744739e4b275e6c22b6695feaac0b161601f))
* GameRepository correction on cast strings ([8e7a39b](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/8e7a39b98341987859475023d65e99cd5d40905d))

## [2.0.1](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v2.0.0...v2.0.1) (2026-06-08)


### Bug Fixes

* add individual workflow secrets inheritance in pipeline.yml ([df69b30](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/df69b30b9fe5ea3a249fe8f09513ca46b5006ea1))
* add permissions to pipeline.yml ([aa78794](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/aa7879438c0211b06a985c5e7467c599a9dcb3d8))
* add SonarCloud analysis to sast-codeql ([6aa3163](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/6aa3163db5ba563b8680c70426e542805dd48219))
* build docker and dast OWASP Zap report path ([adb191c](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/adb191c24d51ecb17babb8a83eaeb1bd87662fc7))
* environment variables inside .yml workflow files ([065cc40](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/065cc40c048c1e263a74b5fd9552323d65939c35))
* identified errors in requirements and DFD's documentation ([6651e0e](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/6651e0eb53ba345b688e6b4478a35f07511437e0))
* pipeline execution order ([27f77ed](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/27f77edb101c76a95c65cc362a77bf2ecbd034b1))
* pipeline semantic error ([228e657](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/228e657ffc0fd01d06d8dcb397b45f3a60ce5c7a))
* reports path for sast .yml workflows ([3ed00cb](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/3ed00cb2ceae1a0e53e2257215673d8fd9570469))
* reports path in workflow .yml files ([f472ba5](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/f472ba53c195f93c559adaf9483962259b0bb46b))
* reports upload folder and pipelines execution order ([35485a1](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/35485a1f23b7cd0933472e5c93c9b43448a8bb9a))
* reports upload path ([2046aa9](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/2046aa9759b1cb3e597ba5fb90276f402474bc46))
* reports upload path ([933c836](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/933c836f20d1070a591f21126ad2a08a6ca6f750))
* reports upload path in sast-code-quality.yml ([fa47407](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/fa47407ba317a1c1e1b7b62e7bdb3c59e88b5a1b))
* SonarCloud report path ([1e8beb9](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/1e8beb918fd1e4dcbf253e9d31c3d3a02d220859))
* swagger endpoint permissions ([a642d99](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/a642d99e6971b7528b7ed2db167cc737e5f7a2ae))

## [2.0.0](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.1.5...v2.0.0) (2026-05-18)


### ⚠ BREAKING CHANGES

* Change the version

### Features

* Change the version ([577160e](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/577160e0591e19972c62a4ec2c42d90fbf9a955c))

## [1.1.5](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.1.4...v1.1.5) (2026-05-18)


### Bug Fixes

* Correction of the KEYCLOAK_CLIENT_ID on the docker-compose.yml  API [#4](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/4) ([9b7a633](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/9b7a63383e444f1a35cd863e0f2b25a68b5e750f))
* Correction of the KEYCLOAK_CLIENT_ID on the proprieties  API [#4](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/4) ([1d5fbfc](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/1d5fbfc51edd682a177dfe1b62c2296ccbd8bf76))

## [1.1.4](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.1.3...v1.1.4) (2026-05-18)


### Bug Fixes

* add KEYCLOAK_CLIENT_ID variable to pipeline workflows [#5](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/5) ([7d3dd31](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/7d3dd31dfadd0b59a9ba48b3d03b5612d7fa19de))

## [1.1.3](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.1.2...v1.1.3) (2026-05-17)


### Bug Fixes

* Change the Tests to be adapted to the new API code [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) ([06c8fc0](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/06c8fc081d4925e829a2933c88d6ff938c045efd))
* Development of more tests for Service,Controller, Validators,Exceptions, Security and Config classes [#12](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/12) ([5c350c3](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/5c350c327ead77c7d254ea203cf57b5c8ffc6ffa))

## [1.1.2](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.1.1...v1.1.2) (2026-05-17)


### Bug Fixes

* second testing the PR label method and the release [#5](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/5) ([d90f75b](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/d90f75bddd404d3340b748760fa1448f5decf9cb))
* update the PR label script [#5](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/5) [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([b5ef457](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/b5ef4570c8b939cffdc41bd2201f3e45d3897084))

## [1.1.1](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.1.0...v1.1.1) (2026-05-17)


### Bug Fixes

* testing the PR label method and the release [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) ([b43684c](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/b43684cfd8e5b3a84ff271274394f208d0b6656f))

## [1.1.0](https://github.com/mei-desofs/desofs2026-wed_nap_2/compare/v1.0.6...v1.1.0) (2026-05-17)


### Features

* Modification of the login endpoint and method, changed the postman collection [#9](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/9) [#10](https://github.com/mei-desofs/desofs2026-wed_nap_2/issues/10) ([6d4e938](https://github.com/mei-desofs/desofs2026-wed_nap_2/commit/6d4e9382262fa231b5513009237076a19a1c2a0d))

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
