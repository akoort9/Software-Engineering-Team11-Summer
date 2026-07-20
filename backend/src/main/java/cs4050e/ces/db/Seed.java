package cs4050e.ces.db;

import cs4050e.ces.db.payment.Card;
import cs4050e.ces.db.theatre.Movie;
import cs4050e.ces.db.users.*;

/** Holds seed variables. */
class Seed {
	
	/** Seed for movies. */
    public static final Movie[] movies = {
	new Movie("Obsession",
		  "horror",
		  "A movie about a guy who wishes that his coworker " +
		  "Nikki loves himself more than anyone else...",
		  "https://image.tmdb.org/t/p/w500/bRwnj8WEKBCvmfeUNOukJPwB43K.jpg",
		  "https://www.youtube.com/watch?v=gMC8kkwbIQQ",
		  9,
		  true),
	new Movie("Chainsaw Man -- The Movie: Reze Arc",
		  "action",
		  "Chainsaw Man faces his deadliest battle yet in " +
		  "a brutal war between devils, hunters and secret enemies.",
		  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ5EDz7NsypfyMnD8hrsTXfQgufO6SfM_Sh8maLxxmHB1ZnCITA",
		  "https://www.youtube.com/watch?v=tAzAhDNdehs",
		  8,
		  true),
	new Movie("Backrooms",
		  "horror",
		  "A strange doorway appears in the basement of a furniture" +
		  "showroom, leading to an endless network of interconnected " +
		  "rooms where time bends and the only thing scarier than " +
		  "getting lost is the sense that something is lying in wait.",
		  "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcTkuhMQYakiLPjCHvm-w7zZKBSuhBc47qZiGboWtihi58KDLEUO",
		  "https://www.youtube.com/watch?v=0HjdiohVOik",
		  8,
		  true),
	new Movie("Jackass: Best and Last",
		  "comedy",
		  "Johnny Knoxville, Steve-O and the rest of the gang return" +
		  " for more outrageous and hilarious stunts.",
		  "https://encrypted-tbn3.gstatic.com/images?q=tbn:ANd9GcROhYXW22TQ5M4XMlX7vLRoiWd2HzjdKnivXiKrg-ntVv4YVGb_",
		  "https://www.youtube.com/watch?v=sNwzFhGwA94",
		  7,
		  false),
	new Movie("Supergirl",
		  "action",
		  "When an unexpected and ruthless adversary strikes too close " +
		  "to home, Supergirl reluctantly joins forces with an unlikely " +
		  "companion for an interstellar journey of vengeance and justice.",
		  "https://m.media-amazon.com/images/M/MV5BMmJkOTE0MWUtY2E5OS00NzEyLWI4NjEtYzQzYzFmMjk5ODE3XkEyXkFqcGc@._V1_FMjpg_UX1000_.jpg",
		  "https://www.youtube.com/watch?v=s1-pfiVMKAs",
		  0,
		  false),
	new Movie("Scary Movie",
		  "comedy",
		  "Twenty-six years after outrunning a suspiciously familiar " +
		  "masked killer, Shorty, Ray, Cindy and Brenda find themselves" +
		  " targeted by another mad slasher.",
		  "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcQo0w5EKGfcrDj-9dZ0LHwyrOXQo_dpX_UvZe8QL9GejXqfnX_E",
		  "https://www.youtube.com/watch?v=cGgWAHXuTKc",
		  5,
		  true),
	new Movie("Star Wars: The Mandalorian and Grogu",
		  "action",
		  "The evil Empire has fallen, but Imperial warlords remain scattered" +
		  " throughout the galaxy. As the fledgling New Republic works to " +
		  "protect everything the Rebellion fought for, they enlist the help " +
		  "of legendary Mandalorian bounty hunter Din Djarin and his young " +
		  "apprentice Grogu.",
		  "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcRW5xu8MvVJr0nWEvBaPMqNhFp4jyiuIZLJLyKnf8C1zc7GkkF_",
		  "https://www.youtube.com/watch?v=IHWlvwu8t1w",
		  7,
		  true),
	new Movie("Michael",
		  "musical",
		  "The story of pop superstar Michael Jackson -- from his " +
		  "extraordinary early days in the Jackson 5 to the visionary " +
		  "artist whose creative ambition fuels a relentless pursuit to " +
		  "become the biggest entertainer in the world.",
		  "https://encrypted-tbn1.gstatic.com/images?q=tbn:ANd9GcTBTeI1-R_3v9u3cnNPGYLJNYEHHyS3ej0io4nGyok_qadL2wct",
		  "https://www.youtube.com/watch?v=3zOLzsbOleM",
		  8,
		  true),
	new Movie("Minions and Monsters",
		  "comedy",
		  "The Minions band together to save the day after unleashing" +
		  " monsters upon an unsuspecting world.",
		  "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSDMajmWqWp9L7uOGjGnN_AO-5zZVzerdqCKH0F3zHV63xkDhL20ze6pPaX9PNvre-4n-WBdw&s=10",
		  "https://www.youtube.com/watch?v=V-O-uBaHk3c",
		  9,
		  false),
	new Movie("Toy Story 5",
		  "comedy",
		  "Buzz, Woody, Jessie and the rest of the gang come face-to-face" +
		  " with Lilypad, a brand-new tablet device that arrives with her " +
		  "own disruptive ideas about what is best for Bonnie. Will " +
		  "playtime ever be the same?",
		  "https://encrypted-tbn2.gstatic.com/images?q=tbn:ANd9GcQhaI_6CovWsQ8lVVArYzJD3BLVGzCwcqKb3MvAajHeBw-T87u5",
		  "https://www.youtube.com/watch?v=c51ND9Hdbw0",
		  8,
		  true)
    };

	public static final User[] users = {
		new Administrator("admin",
    					"admin@ces.com",
    					"admin_password"),
		new Customer("john",
    				"johndaboss@epic.com",
   					"password",
    				"boss",
    				"4530 Sequoia Dr, Oakwood GA 30566",
    				"ACTIVE"),
		new Customer("alfred",
			"ilovemoney@money.com",
			"money$$$",
			"moneybags",
			"123 Nouveau Riche Ln, Marietta GA 30006",
			"ACTIVE"),
		new Customer("jake", 
			"jakerules@jake.com",
			"jakerules123",
			"shepherd",
			"my address lol",
			"INACTIVE")
	};

	public static final Card[] cards = {
		new Card("1234 5678 9101 1121",
			"123 Nouveau Riche Ln, Marietta GA 30006",
			2032, 8),
		new Card("4482 5637 9101 1234",
			"123 Nouveau Riche Ln, Marietta GA 30006",
			2036, 2),
		new Card("6769 5678 9101 8008",
			"123 Nouveau Riche Ln, Marietta GA 30006",
			2028, 4)
	};
} // Seeder
