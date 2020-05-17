
import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author samed
 */
public class Quizz implements Serializable {
ArrayList<Question> EasyQuestions = new ArrayList();
ArrayList<Question> MidQuestions = new ArrayList();
ArrayList<Question> HardQuestions = new ArrayList();

    public Quizz() {
        
        try {

            
			Scanner scanner = new Scanner(new File("myfile.txt"));
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
                                try {
                                 if (line.startsWith("EasyQuestions")) {
                                    
                                EasyQuestions.add(new Question(line.split("\t")[1], Integer.parseInt(line.split("\t")[2])));
                                }else  if (line.startsWith("MidQuestions")) {
                                    
                                MidQuestions.add(new Question(line.split("\t")[1], Integer.parseInt(line.split("\t")[2])));
                                }else  if (line.startsWith("HardQuestions")) {
                                    
                                HardQuestions.add(new Question(line.split("\t")[1], Integer.parseInt(line.split("\t")[2])));
                                }
                            } catch (Exception e) {
                                    System.out.println(line);
                                            break;
                            }
                               
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
        
        
        
        
    }
    
    
    ArrayList<Question> getQuizz(int zorluk){
    ArrayList<Question> questions = new ArrayList<>();
    Random random = new Random();
        switch (zorluk){
            case 0:
                for (int i = 0; i < 4; i++) {
                    int randomInteger = random.nextInt(EasyQuestions.size());
                    questions.add(EasyQuestions.get(randomInteger));
                }
                break;
            case 1:
                for (int i = 0; i < 4; i++) {
                    int randomInteger = random.nextInt(MidQuestions.size());
                    questions.add(MidQuestions.get(randomInteger));
                }
                break;
            case 2:
                for (int i = 0; i < 4; i++) {
                    int randomInteger = random.nextInt(HardQuestions.size());
                    questions.add(HardQuestions.get(randomInteger));
                }
                break;
    }


return questions;
    
}
}