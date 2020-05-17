
import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author samed
 */
public class Question implements Serializable {
    
    String Soru;
    int cevap;
    int WrongCount ;

    public Question(String Soru, int cevap) {
        this.Soru = Soru;
        this.cevap = cevap;
        this.WrongCount = 0 ;
    }

    public int getWrongCount() {
        return WrongCount;
    }

    public void increaseWrongCount() {
        this.WrongCount= this.WrongCount + 1 ;
    }

    public String getSoru() {
        return Soru;
    }

    public void setSoru(String Soru) {
        this.Soru = Soru;
    }

    public int getCevap() {
        return cevap;
    }

    public void setCevap(int cevap) {
        this.cevap = cevap;
    }
    
    
    
}
