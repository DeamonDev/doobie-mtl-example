# Doobie-MTL

In this application I show how to manage (functional) error handling in the context of Doobie. We have database
`manifolds_atlas` with table `algebraic_varieties`. Each variety has its id, name, equation describing it and its euler characteristics. 
If you are not familiar with algebraic topology / geometry you can think about it as an abstract nonsense ;) 

## Prepare database (under postgres user)

To prepare database you just need to run the shell script

```shell
$ ./postgres.sh
```

## Two services

There are two services `ManifoldsAtlas` and `ManifoldsAtlasMTL`. To each of them we inject the DB connection. First one 
deals with errors using ADT `Either[Error, *]`. The problem with this solution is that we need to lift critical errors (e.g. 
database connector problems) to the domain errors. The second approach uses Typelevel's [MTL](https://typelevel.org/cats-mtl/getting-started.html) 
to avoid such a lifting, allowing us to have two error channels. The one for domain errors (managed by ADT) and the one for critical errors 
(injected, in a sense to an effect type). 

This program was inspired after reading very good [article](https://guillaumebogard.dev/posts/functional-error-handling/?utm_source=pocket_mylist) written by Guillaume Bogard. 
