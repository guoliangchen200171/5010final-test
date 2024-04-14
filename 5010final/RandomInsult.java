public class RandomInsult {
    public static String generateRandom() {
        String[] insults = {
                "Hello! How are you welcome to our system.",
                "5010 is a good course.",
                "Holiday is coming.",
                "Have a rest if you feel tired",
                "NEU is a famous university."
        };
        int randomIndex = (int) (Math.random() * insults.length);
        return insults[randomIndex];
    }
}
