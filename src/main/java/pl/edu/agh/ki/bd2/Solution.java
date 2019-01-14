package pl.edu.agh.ki.bd2;


public class Solution {

    private final GraphDatabase graphDatabase = GraphDatabase.createDatabase();

    public void databaseStatistics() {
        System.out.println(graphDatabase.runCypher("CALL db.labels()"));
        System.out.println(graphDatabase.runCypher("CALL db.relationshipTypes()"));
    }

    public void runAllTests() {

        // Wszystkie podpunkty zadania wykonalem przy uzyciu metody runCypher(), nie jest to zbyt 
        // bezpieczne (sql injection) ale na potrzeby tego prostego zadania w zupelnosci wystarcza

        System.out.println("Finding actor by name - Emma Watson:");
        System.out.println(findActorByName("Emma Watson"));

        System.out.println("Finding all movies with a title containing the phrase STAR WARS:");
        System.out.println(findMovieByTitleLike("Star Wars"));

        System.out.println("Finding all movies rated by the user Seungbeom:");
        System.out.println(findRatedMoviesForUser("Seungbeom"));

        System.out.println("Finding all common movies for Emma Watson and Daniel Radcliffe:");
        System.out.println(findCommonMoviesForActors("Emma Watson", "Daniel Radcliffe"));

        System.out.println("Finding movie recommendations for user Sangreal:");
        System.out.println(findMovieRecommendationForUser("Sangreal"));

        // Dalsza czesc to zadania oznaczone w konspekcie jako dodatkowe: 4, 5, ..., 11:

        System.out.println("Create Test Actor 1, Test Movie 1 and merge the with relationship ACTS_IN");
        exercise4();

        System.out.println("Set birthplace and birthdate for the newly added actor");
        exercise5();

        System.out.println("Actors which acted in no less than 6 movies");
        System.out.println(exercise6());

        System.out.println("Average of acting in films for the group of actors who acted in at least 7 movies");
        System.out.println(exercise7());

        System.out.println("All actors who played in at least 5 filmts, directed at least 1 film, ordered by playings");
        System.out.println(exercise8());

        System.out.println("All friends of user Sangreal who rated a film with 3 or more stars");
        System.out.println(exercise9());

        System.out.println("Print all paths between Emma Watson and Tom Hanks of length max 5 with no Movie nodes in them");
        System.out.println(exercise10());

        System.out.println("Now running exercise 11:");
        exercise11();

    }

    private String findActorByName(final String actorName) {
        String res = graphDatabase.runCypher(
            "MATCH (a:Actor) WHERE a.name = \"" + actorName + "\" RETURN a.name"
        );
        return res;
    }

    private String findMovieByTitleLike(final String movieName) {
        String res = graphDatabase.runCypher(
            "MATCH (m:Movie) WHERE m.title =~ '.*" + movieName + ".*' RETURN m.title"
        );
        return res;
    }

    private String findRatedMoviesForUser(final String userLogin) {
        String res = graphDatabase.runCypher(
            "MATCH (m:Movie) <-[:RATED]- (u:User) WHERE u.name = \"" + userLogin + "\" RETURN m.title"
        );
        return res;
    }

    private String findCommonMoviesForActors(String actorOne, String actrorTwo) {
        String res = graphDatabase.runCypher(
            "MATCH (a:Actor {name: \"" + actorOne + "\"})-[:ACTS_IN]->(m:Movie)<-[:ACTS_IN]-(b:Actor {name: \"" + actrorTwo + "\"}) RETURN m.title"
        );
        return res;
    }

    private String findMovieRecommendationForUser(final String userLogin) {
        String res = graphDatabase.runCypher(
            "MATCH (u1:User {name: \"" + userLogin + "\"}) -[r1:RATED]-> (m1:Movie) <-[r2:RATED]- (u2:User) -[r3:RATED]-> (m2:Movie)" +
            "WHERE r1.stars > 3 AND r2.stars > 3 AND r3.stars > 3 AND NOT EXISTS( (u1)-[:RATED]->(m2) ) RETURN m2.title"
        );
        return res;
    }

    private void exercise4() {
        graphDatabase.runCypher(
            "CREATE (m:Movie {title : 'Test Movie 1'})"
        );
        graphDatabase.runCypher(
            "CREATE (m:Actor {name : 'Test Actor 1'})"
        );
        graphDatabase.runCypher(
            "MATCH (m:Movie {title: 'Test Movie 1'}), " +
            "(a:Actor {name: 'Test Actor 1'}) " +
            "MERGE (a)-[r:ACTS_IN]->(m)"
        );
    }

    private void exercise5() {
        graphDatabase.runCypher(
            "MATCH (a:Actor {name : 'Test Actor 1'}) SET a.birthplace = 'Hogwart'"
        );
        graphDatabase.runCypher(
            "MATCH (a:Actor {name : 'Test Actor 1'}) SET a.birthdate = 162255600000"
        );
    }

    private String exercise6() {
        return graphDatabase.runCypher(
            "MATCH (a:Actor) -[:ACTS_IN]-> (m:Movie) WITH SIZE(COLLECT(m)) AS NumberOfMovies, a.name AS Name WHERE NumberOfMovies >= 6 RETURN Name, NumberOfMovies LIMIT 25"
        );
    }

    private String exercise7() {
        return graphDatabase.runCypher(
            "MATCH (a:Actor) -[:ACTS_IN]-> (m:Movie) WITH SIZE(COLLECT(m)) AS NumberOfMovies, a.name AS Name WHERE NumberOfMovies >= 7 RETURN AVG(NumberOfMovies) LIMIT 25"
        );
    }

    private String exercise8() {
        return graphDatabase.runCypher(
            "MATCH (n:Movie) <-[:DIRECTED]- (a:Actor) -[:ACTS_IN]-> (m:Movie) WITH SIZE(COLLECT(m)) AS NumberOfMovies, " +
            "SIZE(COLLECT(n)) AS MoviesDirected, a.name AS Name WHERE NumberOfMovies >= 5 AND MoviesDirected >= 1 " + 
            "RETURN Name, NumberOfMovies ORDER BY NumberOfMovies LIMIT 25"
        );
    }

    private String exercise9() {
        return graphDatabase.runCypher(
            "MATCH (u:User)-[:FRIEND]-(f:User)-[r:RATED]->(m:Movie) WHERE r.stars >= 3 AND u.name = \"Sangreal\" RETURN f.name, m.title, r.stars"
        );
    }

    private String exercise10() {
        return graphDatabase.runCypher(
                "MATCH Path = (a1:Actor {name: \"Tom Hanks\"})-[*1..5]-(a2:Actor {name: \"Emma Watson\"}) WITH COLLECT(Path) AS Paths RETURN [p IN Paths WHERE NONE(x IN nodes(p) WHERE x:Movie) | p]"
        );
    }

    private void exercise11() {

        // Na poczatku wykonalem to zadanie przez klienta w przegladarce, gdzie opcja EXPLAIN faktycznie wyswietlala czas wykonania
        // i wtedy faktycznie wykonanie ponizszych zapytan z indeksami bylo ZNACZNIE szybsze, tutaj uzylem mierzenie czasu przez Jave
        // co nie jest zbyt miarodajne, zas runCypyher() nie wyswietla calego wyniku zapytania EXPLAIN

        graphDatabase.runCypher("DROP INDEX ON :Actor(name)");

        System.out.println("Finding actor Tom Hanks by name without index:");
        long startTime = System.nanoTime();
        String res = graphDatabase.runCypher("PROFILE MATCH (a:Actor) WHERE a.name = \"Tom Hanks\" RETURN a.name");
        long endTime = System.nanoTime();
        System.out.println("Result:");
        System.out.println(res);
        System.out.println("It took: " + (endTime - startTime) + " nanoseconds.");

        System.out.println("Now creating an index on Actor.name");
        graphDatabase.runCypher("CREATE INDEX ON :Actor(name)");

        System.out.println("Finding actor Tom Hanks by name with index:");
        startTime = System.nanoTime();
        res = graphDatabase.runCypher("PROFILE MATCH (a:Actor) USING INDEX a:Actor(name) WHERE a.name = \"Tom Hanks\" RETURN a.name");
        endTime = System.nanoTime();
        System.out.println("Result:");
        System.out.println(res);
        System.out.println("It took: " + (endTime - startTime) + " nanoseconds.");

        graphDatabase.runCypher("DROP INDEX ON :Actor(name)");

        System.out.println("Now finding shortestPath between Kevin Bacon and Tomasz Karolak without index:");
        startTime = System.nanoTime();
        res = graphDatabase.runCypher(
                "PROFILE MATCH p=shortestPath((a1:Actor {name:\"Kevin Bacon\"})-[*]-(a2:Actor {name:\"Tomasz Karolak\"})) RETURN p");
        endTime = System.nanoTime();
        System.out.println("Result:");
        System.out.println(res);
        System.out.println("It took: " + (endTime - startTime) + " nanoseconds.");

        graphDatabase.runCypher("CREATE INDEX ON :Actor(name)");

        System.out.println("Now finding shortestPath between Kevin Bacon and Tomasz Karolak with index:");
        startTime = System.nanoTime();
        res = graphDatabase.runCypher(
                "PROFILE MATCH p=shortestPath((a1:Actor {name:\"Kevin Bacon\"})-[*]-(a2:Actor {name:\"Tomasz Karolak\"})) RETURN p");
        endTime = System.nanoTime();
        System.out.println("Result:");
        System.out.println(res);
        System.out.println("It took: " + (endTime - startTime) + " nanoseconds.");

    }

}
