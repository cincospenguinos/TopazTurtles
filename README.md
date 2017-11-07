# NLP Project -- Topaz Turtles

Here's the parts we will need:

- [ ] Something that grabs the correct answers from the gold standard answers
- [ ] Something that extracts features from a text and outputs it in a format 
      that a machine learning library can handle
- [x] A Main class that takes the proper input and kicks off everything
- [ ] PoS tagger and other lower level NLP tools
- [ ] A Named Entity Recognizer of some sort (trust me, this will help enormously)
- [x] Machine Learning library of some sort, preferably through Java

## TODO

- [ ] Start experimenting with classifiers using Apache's ML library
- [ ] Code up all of the features that you've observed
- [ ] Setup method of grabbing documents and pulling in their texts

## Decisions to make

* How should we look at all the different files?
* Should we store each text in a database? Something like SQLite and then just query it for things?
* What's our pipeline?

## Notes

* Our machine learning library is `org.apache.spark.mllib`. Documentation is [here](http://spark.apache.org/docs/latest/mllib-guide.html).
  We picked this one because it allows us to use local vectors and the like. Trust me; it will help tremendously.
* [Here](https://stanfordnlp.github.io/CoreNLP/)'s some docs on StanfordNLP Core. And [here](https://github.com/stanfordnlp/CoreNLP)'s
  the GitHub page.

## Potential Resources

* [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/)
* [WordNet](https://wordnet.princeton.edu/)
* [MLlib by Apache](http://spark.apache.org/mllib/)
* [MALLET](http://mallet.cs.umass.edu/)
* [Datamuse](http://www.datamuse.com/api/) --> Shows relationships between words. Can be used to find potential weapons.