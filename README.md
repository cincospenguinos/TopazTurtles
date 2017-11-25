# NLP Project -- Topaz Turtles

Andre LaFleur and Quinn Luck's NLP project.

## HOW TO RUN

Simply extract the file, cd into the directory, and run the infoextract script.

```
tar -xzvf topaz_turtles.tar.gz
cd topaz_turtles
./infoextract <filename>
```

```shell
tar -xzvf topaz_turtles.tar.gz
cd topaz_turtles
./infoextract <filename>
```

### Information the TAs need to know

1) We used the following resources:
    * [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/) for NLP related tasks. This is included in the build,
    but we don't have the functionality for it yet (still debugging some things.)
    * [Datamuse](http://www.datamuse.com/api/) A really simple API that provides words that mean like other words, as
    well as a few other things. The DataMuse class in the source code is the only spot it's used.
2) It doesn't take too long to process a single document (yet.) It takes about a minute for everything to run.
3) Contributions
    * Andre LaFleur
        * Setup the project and build file
        * Created setup function and ensured proper data gathering and storage
        * Wrote the infoextract BASH script
        * Setup use of machine learning to classify the Incident slot for each document
    * Quinn Luck
        * Wrote a parser to extract each individual document from the provided file
        
4) Right now, it only labels the incident type and the ID. I had a lot of trouble getting StanfordNLP to work, which
is an important part of figuring out locations and people of interest. That's my next big goal with this project. 
I'm embarrassed to admit that that's all we've done, but it's true. The good news is that the classification of the
Incident slot works. More features will be included in the final submission that should improve its accuracy, and
intelligent guesses will be given for each of the other slots as well.

For a more detailed account of the separation of labor on this project, please 
[look at our github repo](https://github.com/cincospenguinos/TopazTurtles).

## Potential Resources

* [Stanford CoreNLP](https://stanfordnlp.github.io/CoreNLP/)
* [WordNet](https://wordnet.princeton.edu/)
* [MLlib by Apache](http://spark.apache.org/mllib/)
* [MALLET](http://mallet.cs.umass.edu/)
* [Datamuse](http://www.datamuse.com/api/) --> Shows relationships between words. Can be used to find potential weapons.