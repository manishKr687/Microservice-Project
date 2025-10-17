import java.util.*;

public class TicTacToe {

    public static Boolean checkWin(List<List<Character>> board, Character currentPlayer){
        for(int i=0; i<3; i++){
            if(board.get(i).get(0) ==currentPlayer && board.get(i).get(1)==currentPlayer && board.get(i).get(2)==currentPlayer){
                return true;
            }
            if(board.get(0).get(i) ==currentPlayer && board.get(1).get(i)==currentPlayer && board.get(2).get(i)==currentPlayer){
                return true;
            }
        }
        if(board.get(0).get(0)==currentPlayer && board.get(1).get(1)==currentPlayer && board.get(2).get(2)==currentPlayer){
            return true;
        }
        if(board.get(0).get(2)==currentPlayer && board.get(1).get(1)==currentPlayer && board.get(2).get(0)==currentPlayer){
            return true;
        }
        return false;
    }
    public static Boolean isFull(List<List<Character>> board){
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                if(board.get(i).get(j)==' '){
                    return false;
                }
            }
        }
        return true;
    }

    public static void printBoard(List<List<Character>> board){
        for(int i=0;i<3;i++){
            System.out.print("|");
            for(int j=0;j<3;j++){
                System.out.print(board.get(i).get(j)+"|");
            }
            System.out.println();
        }
    }




    public static void main(String args[]){
        //initialize board
        List<List<Character>> board = new ArrayList<>();
        for(int i=0;i<3;i++){
            List<Character> row = new ArrayList<>();
            for(int j=0;j<3;j++){
                row.add(' ');
            }
            board.add(row);
        }

        //declare CurrentPlayer
        Character currentPlayer='X';

        //choose position in grid by player
        while(true){
            printBoard(board);
            Scanner sc = new Scanner(System.in);
            Integer i=sc.nextInt();
            Integer j=sc.nextInt();
            board.get(i).set(j,currentPlayer);


            if(checkWin(board,currentPlayer)){
                System.out.println(currentPlayer + " Win! ");
                break;
            }
            if(isFull(board)){
                System.out.println("Draw!");
                break;
            }
            currentPlayer = (currentPlayer == 'X') ? 'Y' : 'X';
        }

    }

}
