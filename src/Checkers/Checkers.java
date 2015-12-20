// TODO Winning/losing
// TODO restart/new game functions
// TODO OPPONENT AI?
// Derek Watring
// NOTE: Information on each method is available at https://github.com/dwatring/Java-Checkers/wiki
package Checkers;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Checkers extends JPanel implements ActionListener, MouseListener {
	private static final long serialVersionUID = 1L; //Why? TODO GOOGLE
	public static int width = 720, height = width; //square parameters. Optimized for any square resolution TODO any resolution to be squared
	public static final int tileSize = width/8; //8 Tiles for checkers board
	public static final int numTilesPerRow = width/tileSize;
	public static int[][] baseGameData = new int[numTilesPerRow][numTilesPerRow]; //Stores 8x8 board layout
	public static int[][] gameData = new int[numTilesPerRow][numTilesPerRow]; //Stores piece data in an 8x8
	public static final int EMPTY = 0, RED = 1, RED_KING = 2, WHITE = 3, WHITE_KING = 4; //Values for gameData
	
	public boolean gameInProgress = true;
	public int currentPlayer = RED;
	public boolean inPlay = false; //Is there a move function processing?
	public static int[][] availablePlays = new int[numTilesPerRow][numTilesPerRow]; //Stores available plays in an 8x8
	public int storedRow, storedCol;
	public boolean isJump = false;
	static BufferedImage crownImage = null;
	
	//TODO can I eliminate isJump or inPlay?
	
	public static void main(String[] args){
		try {
			crownImage = ImageIO.read(new File("crown.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Checkers();
	}
	
	public Checkers(){
		window(width, height, this);
		initializeBoard();
		repaint(); // This is included in the JVM. Runs paint.
	}
	
	public void gameOver(){
	      gameInProgress = false;
	      System.exit(1);
	}
	
	public void window(int width, int height, Checkers game){ //draw the frame and add exit functionality
		JFrame frame = new JFrame();
		frame.setSize(width, height);
		frame.setIconImage(crownImage);
		frame.setBackground(Color.cyan);
		frame.setLocationRelativeTo(null);
		frame.pack();
		Insets insets = frame.getInsets();
		int frameLeftBorder = insets.left;
		int frameRightBorder = insets.right;
		int frameTopBorder = insets.top;
		int frameBottomBorder = insets.bottom;
		frame.setPreferredSize(new Dimension(width + frameLeftBorder + frameRightBorder, height + frameBottomBorder + frameTopBorder));
		frame.setMaximumSize(new Dimension(width + frameLeftBorder + frameRightBorder, height + frameBottomBorder + frameTopBorder));
		frame.setMinimumSize(new Dimension(width + frameLeftBorder + frameRightBorder, height + frameBottomBorder + frameTopBorder));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addMouseListener(this);
		frame.requestFocus();
		frame.setVisible(true);
		frame.add(game);
	}
	
	public void initializeBoard(){
		//UPDATE THE STARTING POSITIONS
				for(int col=0; col < (numTilesPerRow); col+=2){
					gameData[5][col] = RED;
					gameData[7][col] = RED;
				}
				for(int col=1; col < (numTilesPerRow); col+=2)
					gameData[6][col] = RED;
				for(int col=1; col < (numTilesPerRow); col+=2){
					gameData[0][col] = WHITE;
					gameData[2][col] = WHITE;
				}
				for(int col=0; col < (numTilesPerRow); col+=2)
					gameData[1][col] = WHITE;
	}
	
	public static void drawPiece(int col, int row, Graphics g, Color color){
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setColor(color);
		// These 2 and 4 values are arbitrary values that compensate for a smaller piece size than tileSize
		g.fillOval((col*tileSize)+2, (row*tileSize)+2, tileSize-4, tileSize-4);
	}
	
	public void paint(Graphics g){ // This method paints the board
		//PRINT THE BOARD & PIECES
		super.paintComponent(g);
		long timeStart = System.currentTimeMillis();
		for(int row = 0; row < numTilesPerRow; row++){
			for(int col = 0; col < numTilesPerRow; col++){
				if((row%2 == 0 && col%2 == 0) || (row%2 != 0 && col%2 != 0)){ // This assigns the checkerboard pattern
					baseGameData[row][col] = 0;
					g.setColor(Color.gray);
					g.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
				}
				else{
					baseGameData[row][col] = 1;
					g.setColor(Color.darkGray);
					g.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
				}
				if(checkTeamPiece(gameData, row, col) ==  true){			
					g.setColor(Color.darkGray.darker());
					g.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
				}
				if(availablePlays[row][col] == 1){
					g.setColor(Color.CYAN.darker());
					g.fillRect(col*tileSize, row*tileSize, tileSize, tileSize);
				}
				if(gameData[row][col] == WHITE)
					drawPiece(col, row, g, Color.white);
				else if(gameData[row][col] == WHITE_KING){
					drawPiece(col, row, g, Color.white);
					g.drawImage(crownImage, (col*tileSize)+6, (row*tileSize)+6, tileSize-12, tileSize-12, null);
				}
				else if(gameData[row][col] == RED)
					drawPiece(col, row, g, Color.red);
				else if(gameData[row][col] == RED_KING){
					drawPiece(col, row, g, Color.red);
				g.drawImage(crownImage, (col*tileSize)+6, (row*tileSize)+6, tileSize-12, tileSize-12, null);
				}
			}
		}
		long timeEnd = System.currentTimeMillis();
		System.out.println(timeEnd - timeStart);
	}
	
	public void resetPlay(){
		storedCol = 0;
		storedRow = 0;
		inPlay = false;
		isJump = false;
		for(int row = 0; row < numTilesPerRow; row++){
			for(int col = 0; col < numTilesPerRow; col++){
				availablePlays[row][col] = 0;
			}
		}
		repaint();
	}
	
	public void mousePressed(java.awt.event.MouseEvent evt) {
    	int col = evt.getX() / tileSize;
        int row = evt.getY() / tileSize;
		if(inPlay == true && availablePlays[row][col] == 1){
			makeMove(gameData, row, col, storedRow, storedCol);
		}
		if(inPlay == true && availablePlays[row][col] == 0){
			resetPlay();
		}
		if(inPlay == false && gameData[row][col] != 0){
		    if (gameInProgress == false)
		    	System.exit(1);
		    else {
		        storedCol = col;
		        storedRow = row;
		        getAvailablePlays(gameData, row, col);
			}
		}
	}
	
	public void swapPlayer(){
		if(currentPlayer == RED)
			currentPlayer = WHITE;
		else currentPlayer = RED;
	}
	
	public void makeMove(int[][] gameData, int row, int col, int storedRow, int storedCol){ // is gameData needed to be in these functions?
		int x = gameData[storedRow][storedCol]; //change the piece to new tile
		gameData[row][col] = x;
		if(isJump == true)
			removePiece(gameData, row, col, storedRow, storedCol);
		makeKing(gameData, row, col, storedRow, storedCol);
		gameData[storedRow][storedCol] = EMPTY; //change old piece location to EMPTY
		swapPlayer();
		resetPlay();
	}
	
	public void makeKing(int[][] gameData, int row, int col, int storedRow, int storedCol){
		if(gameData[row][col] == RED && row == 0)
			gameData[row][col] = RED_KING;
		else if(gameData[row][col] == WHITE && row == numTilesPerRow-1)
			gameData[row][col] = WHITE_KING;
		else return;
	}
	
	public void removePiece(int[][] gameData, int row, int col, int storedRow, int storedCol){ //might be a better way to do this, but detects position of opponent piece based on destination and original position
		int pieceRow = -1; 
		int pieceCol = -1;
		if(col > storedCol && row > storedRow){
			pieceRow = row-1;
			pieceCol = col-1;
		}
		if(col > storedCol && row < storedRow){
			pieceRow = row+1;
			pieceCol = col-1;
		}
		if(col < storedCol && row > storedRow){
			pieceRow = row-1;
			pieceCol = col+1;
		}
		if(col < storedCol && row < storedRow){
			pieceRow = row+1;
			pieceCol = col+1;
		}
		gameData[pieceRow][pieceCol] = EMPTY;
	}
	
	public void getAvailablePlays(int[][] gameData, int row, int col){
		inPlay = true;
		if((checkTeamPiece(gameData, row, col) == true)){ //checks if the piece is assigned to the current player
			if(gameData[row][col] == RED){  // only goes north, checks the row above it's own
				getUp(row, col);
			}
			if(gameData[row][col] == WHITE){ // only goes south, checks the row below it's own
				getDown(row, col);
			}
			if(gameData[row][col] == RED_KING || gameData[row][col] == WHITE_KING){ // Goes up OR down 1 row below it's own
				getUp(row, col);
			  //getUp(row, col);
				getDown(row, col); // GET UP GET UP AND GET DOWN
			}
		}
		repaint();
	}
	
	public void getUp(int row, int col){
		int rowUp = row-1;
		if(col == 0 && row != 0){
			for(int i = col; i < col+2; i++){
				if(gameData[row][col] != 0 && gameData[rowUp][i] != 0){
					if(canJump(row, col, rowUp, i) == true){
						int jumpRow = getJumpRow(row, col, rowUp, i);
						int jumpCol = getJumpCol(row, col, rowUp, i);
						availablePlays[jumpRow][jumpCol] = 1;
					}
				}
				else if(baseGameData[rowUp][i] == 1 && gameData[rowUp][i] == 0)
					availablePlays[rowUp][i] = 1;
			}
		}
		else if(col == numTilesPerRow - 1 && row != 0){
			for(int i = col-1; i < col+1; i++){
				if(gameData[row][col] != 0 && gameData[rowUp][i] != 0){
					if(canJump(row, col, rowUp, i) == true){
						int jumpRow = getJumpRow(row, col, rowUp, i);
						int jumpCol = getJumpCol(row, col, rowUp, i);
						availablePlays[jumpRow][jumpCol] = 1;
					}
				}
				else if(baseGameData[rowUp][i] == 1 && gameData[rowUp][i] == 0)
					availablePlays[rowUp][i] = 1;
			}
		}
		else if(col != numTilesPerRow -1 && col != 0 && row != 0){
			for(int i = col-1; i < col+2; i++){
				if(gameData[row][col] != 0 && gameData[rowUp][i] != 0){
					if(canJump(row, col, rowUp, i) == true){
						int jumpRow = getJumpRow(row, col, rowUp, i);
						int jumpCol = getJumpCol(row, col, rowUp, i);
						availablePlays[jumpRow][jumpCol] = 1;
					}
				}
				else if(baseGameData[rowUp][i] == 1 && gameData[rowUp][i] == 0)
					availablePlays[rowUp][i] = 1;
			}
		}
	}
	
	public void getDown(int row, int col){
		int rowDown = row+1;
		if(col == 0 && row != numTilesPerRow-1){
			for(int i = col; i < col+2; i++){
				if(gameData[row][col] != 0 && gameData[rowDown][i] != 0){
					if(canJump(row, col, rowDown, i) == true){
						int jumpRow = getJumpRow(row, col, rowDown, i);
						int jumpCol = getJumpCol(row, col, rowDown, i);
						availablePlays[jumpRow][jumpCol] = 1;
					}
				}
				else if(baseGameData[rowDown][i] == 1 && gameData[rowDown][i] == 0)
					availablePlays[rowDown][i] = 1;
			}
		}
		else if(col == numTilesPerRow - 1 && row != numTilesPerRow-1){
			for(int i = col-1; i < col+1; i++){
				if(gameData[row][col] != 0 && gameData[rowDown][i] != 0){
					if(canJump(row, col, rowDown, i) == true){
						int jumpRow = getJumpRow(row, col, rowDown, i);
						int jumpCol = getJumpCol(row, col, rowDown, i);
						availablePlays[jumpRow][jumpCol] = 1;
					}
				}
				else if(baseGameData[rowDown][i] == 1 && gameData[rowDown][i] == 0)
					availablePlays[rowDown][i] = 1;
			}
		}
		else if(col != numTilesPerRow-1 && col != 0 && row != numTilesPerRow-1){
			for(int i = col-1; i < col+2; i++){
				if(gameData[row][col] != 0 && gameData[rowDown][i] != 0){
					if(canJump(row, col, rowDown, i) == true){
						int jumpRow = getJumpRow(row, col, rowDown, i);
						int jumpCol = getJumpCol(row, col, rowDown, i);
						availablePlays[jumpRow][jumpCol] = 1;
					}
				}
				else if(baseGameData[rowDown][i] == 1 && gameData[rowDown][i] == 0)
					availablePlays[rowDown][i] = 1;
			}
		}
	}
	
	public boolean checkTeamPiece(int[][] gameData, int row, int col){
		if(currentPlayer == RED && (gameData[row][col] == RED || gameData[row][col] == RED_KING)) //bottom
			return true;
		if(currentPlayer == WHITE && (gameData[row][col] == WHITE || gameData[row][col] == WHITE_KING)) //top
			return true;
		else
			return false;
	}
	
	public boolean canJump(int row, int col, int row2, int col2){
		//Steps for checking if canJump is true: determine piece within movement. Then check if its an opponent piece, then if the space behind it is empty
		//and in bounds
		// 4 conditions based on column and row relations to the other piece
		int toRow = -1, toCol = -1;
		if(((gameData[row][col] == WHITE || gameData[row][col] == WHITE_KING) 
				&& (gameData[row2][col2] == RED || gameData[row2][col2] == RED_KING)) 
				|| (gameData[row][col] == RED || gameData[row][col] == RED_KING) 
				&& (gameData[row2][col2] == WHITE || gameData[row2][col2] == WHITE_KING)){ 
			//If the piece is white/red and opponent piece is opposite TODO fix this if. It's so ugly
			if(col > col2 && row > row2){
				toRow = row-2;
				toCol = col-2;
			}
			else if(col > col2 && row < row2){
				toRow = row+2;
				toCol = col-2;
			}
			else if(col < col2 && row > row2){
				toRow = row-2;
				toCol = col+2;
			}
			else if(col < col2 && row < row2){
				toRow = row+2;
				toCol = col+2;
			}
		    if (toRow < 0 || toRow >= numTilesPerRow || toCol < 0 || toCol >= numTilesPerRow) //check board outofbounds
		        return false;
		    
		    if(gameData[toRow][toCol] == 0){
		    	isJump = true;
		    	return true;
		    }
		}
		return false;
	}
	
	public int getJumpCol(int row, int col, int row2, int col2){
		int toCol = 0;
		if(col > col2 && row > row2 && gameData[row-2][col-2] == 0){
			toCol = col-2;
		}
		if(col > col2 && row < row2 && gameData[row+2][col-2] == 0){
			toCol = col-2;
		}
		if(col < col2 && row > row2 && gameData[row-2][col+2] == 0){
			toCol = col+2;
		}
		if(col < col2 && row < row2 && gameData[row+2][col+2] == 0){
			toCol = col+2;
		}
		return toCol;
	}
	
	public int getJumpRow(int row, int col, int row2, int col2){
		if(col > col2 && row > row2 && gameData[row-2][col-2] == 0){
			return row-2;
		}
		else if(col > col2 && row < row2 && gameData[row+2][col-2] == 0){
			return row+2;
		}
		else if(col < col2 && row > row2 && gameData[row-2][col+2] == 0){
			return row-2;
		}
		else
			return row+2;
	}
	
	// Methods that must be included for some reason? WHY
	public void mouseClicked(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void actionPerformed(ActionEvent e) {}
}
