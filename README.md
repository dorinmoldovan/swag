# SWAG - Semantic Web Application Generator (A library for using ontologies as web services)

## Using SWAG

1. swag.config contains the project's configuration.
2. API_TYPE can be JENA or D2RQ.
3. The generated files will be saved at the location specified in the configuration.
4. In the case D2RQ is used an empty database with the specified name must already exist and the tables will be 
generated automatically.
5. The package ro.tuc.dsrl.swag.model.diagnostic contains examples of how this API is used.

## Built With
* [Maven](https://maven.apache.org/)
* [Protege](https://protege.stanford.edu/)
* [arq](http://www.java2s.com/Code/Jar/a/Downloadarq285jar.htm)
* [d2rq](http://d2rq.org/)
* [d2rqUpdate](https://github.com/VadimEisenberg/d2rqUpdate)
* [jgrapht](http://www.jgrapht.org/)
* [jsqlparser](https://github.com/JSQLParser/JSqlParser)

## Use Case

Weight diagnostic detection.

![Diagnostic Ontology](src/main/resources/ProtegeDiagram.jpg?raw=true "Title")

## Authors

Dorin Moldovan & Claudia Pop & Marcel Antal & Tudor Cioara & Ionut Anghel & Ioan Salomie

See more details about our research activity at the following link [DSRL](http://dsrl.coned.utcluj.ro/).

## License

This project is licensed under the GPL License, see the [LICENSE](LICENSE) file for details.

## Research Articles

[1] D. Moldovan, C. Pop, M. Antal, T. Cioara, I. Anghel, I. Salomie, "SWAG: Semantic web application generator - a library for using ontologies as web services", 2016 IEEE 12th International Conference on Intelligent Computer Communication and Processing (ICCP), Cluj-Napoca, Cluj, Romania, 8-10 September 2016. [[CrossRef](https://ieeexplore.ieee.org/document/7737130)]

[2] C. Pop, D. Moldovan, M. Antal, D. Valea, T. Cioara, I. Anghel, I. Salomie, "M2O: A library for using ontologies in software engineering", 2015 IEEE International Conference on Intelligent Computer Communication and Processing (ICCP), Cluj-Napoca, Cluj, Romania, 3-5 September 2015. [[CrossRef](https://ieeexplore.ieee.org/document/7312608)]
