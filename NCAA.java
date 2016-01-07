import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class NCAA {
	public static void main(String args[]) {
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
			int slot = 18;
			int home = 12;
			int away = 12;
			
			double[][] matrix = {{0,1,1,1,0,1,1,1,0,1,1,0},{1,0,1,1,1,1,1,1,0,1,0,0},
					{1,1,0,1,1,1,0,1,0,0,1,1},{1,1,0,0,1,1,1,0,1,0,1,1},
					{1,1,1,1,0,0,0,1,1,0,1,1},{0,1,1,1,1,0,1,1,1,1,0,0},
					{1,0,1,1,1,0,0,0,1,1,1,1},{0,0,1,1,1,0,1,0,1,1,1,1},
					{1,1,1,0,0,1,0,1,0,1,1,1},{0,0,1,1,1,1,1,0,1,0,1,1},
					{1,1,0,0,0,1,1,1,1,1,0,1},{1,1,0,0,1,1,1,1,1,1,0,0}};
			
			int[][] matrix2 = {{6,10,1,2,3},{4,3,0,2,5},{7,5,0,1,4},{6,1,0,4,5},
					{1,11,2,3,7},{2,9,1,3,8},{0,3,9,10,11},{2,8,4,10,11},
					{7,11,5,9,10},{10,5,6,8,11},{9,0,6,7,8},{4,8,6,7,9}};
			
		    GRBVar[][][] vars = new GRBVar[slot][home][away];
		    GRBVar[][] y = new GRBVar[60][2];
		    
		    // Initializing variables
		    for(int i=0; i < slot; i++) {
		    	for(int j=0; j < home; j++) {
		    		for(int k=0; k < away; k++) {
		    			String st = "G_" + String.valueOf(i) + "_" + 
			    		String.valueOf(j) + "_" + String.valueOf(k);
			    		vars[i][j][k] = model.addVar(0, 1.0, 3, GRB.BINARY, st);
		    		}
		    	}
		    }		    
		    model.update();
		    
		    // Initializing binaries
		    for(int i=0; i < 60; i++) {
		    	for(int j=0; j < 2; j++) {
	    			String st = "B_" + String.valueOf(i) + "_" + 
		    		String.valueOf(j);
		    		y[i][j] = model.addVar(0, 1.0, 3, GRB.BINARY, st);
		    	}
		    }
		    model.update();
			
		    GRBLinExpr expr;
		    GRBLinExpr expr1;
		    GRBLinExpr expr2;
		    GRBLinExpr expr3;
		    GRBLinExpr expr4;
		    GRBLinExpr expr5;
		    
		    // Table Constraints 
		    for(int j=0; j < home; j++) {
		    	for(int k=0; k < away; k++) {
				    expr = new GRBLinExpr();
		    		for(int i=0; i < slot; i++) {
		    			expr.addTerm(1.0, vars[i][j][k]);
		    		}
		    		String st = "TABLE_"+j+"_"+k;
		    		model.addConstr(expr, GRB.EQUAL, matrix[j][k], st);
		    	}
		    }
		    model.update();    
		    
		    // Byes
		    expr = new GRBLinExpr();
		    for(int k=0; k < home; k++) {
		    	expr.addTerm(1.0, vars[0][2][k]);
		    	expr.addTerm(1.0, vars[0][7][k]);
		    	expr.addTerm(1.0, vars[7][7][k]);
		    	expr.addTerm(1.0, vars[11][4][k]);
		    	expr.addTerm(1.0, vars[15][2][k]);
		    	expr.addTerm(1.0, vars[1][9][k]);
		    }
		    for(int j=0; j < home; j++) {
		    	expr.addTerm(1.0, vars[0][j][2]);
		    	expr.addTerm(1.0, vars[0][j][7]);
		    	expr.addTerm(1.0, vars[7][j][7]);
		    	expr.addTerm(1.0, vars[11][j][4]);
		    	expr.addTerm(1.0, vars[15][j][2]);
		    	expr.addTerm(1.0, vars[1][j][9]);
		    }
		    model.addConstr(expr, GRB.EQUAL, 0, "Byes");
		    model.update();
		    
		    // Each team can play 1 match per week
		    for(int i=0; i < slot; i++) {
		    	for(int j=0; j < home; j++) {
				    expr = new GRBLinExpr();
		    		for(int k=0; k < away; k++) {
			    		expr.addTerm(1.0, vars[i][j][k]);
			    		expr.addTerm(1.0, vars[i][k][j]);
		    		}
		    		String st = "1618_"+i+"_"+j;
		    		model.addConstr(expr, GRB.LESS_EQUAL, 1, st);
		    	}
		    }
		    model.update();

		    // should play 16 games
		    for(int j=0; j < home; j++) {
		    	expr = new GRBLinExpr();
		    	for(int i=0; i < slot; i++) {
		    		for(int k=0; k < away; k++) {
		    			expr.addTerm(1.0, vars[i][j][k]);
		    			expr.addTerm(1.0, vars[i][k][j]);
		    		}
		    	}
		    	String st = "SHLDPLY16_"+j;
		    	model.addConstr(expr, GRB.EQUAL, 16, st);
		    }
		    model.update();
		    
		    // cannot play 3 consecutive home matches
		    for(int i=0; i < 16; i++) {
		    	for(int j=0; j < home; j++) {
		    		expr = new GRBLinExpr();	
		    		for(int k=0; k < away; k++) {
		    			expr.addTerm(1.0, vars[i][j][k]);
		    			expr.addTerm(1.0, vars[i+1][j][k]);
		    			expr.addTerm(1.0, vars[i+2][j][k]);
		    		}
		    		String st = "HOME3_"+i+"_"+j;
		    		model.addConstr(expr, GRB.LESS_EQUAL, 2, st);
		    	}
		    }	
		    model.update();
		    
		    // cannot play 3 consecutive away matches
		    for(int i=0; i < 16; i++) {
		    	for(int k=0; k < away; k++) {
		    		expr = new GRBLinExpr();
		    		for(int j=0; j < home; j++) {
		    			expr.addTerm(1.0, vars[i][j][k]);
		    			expr.addTerm(1.0, vars[i+1][j][k]);
		    			expr.addTerm(1.0, vars[i+2][j][k]);
		    		}
		    		String st = "AWAY3_"+i+"_"+k;
		    		model.addConstr(expr, GRB.LESS_EQUAL, 2, st);
		    	}
		    }
		    model.update();
		    
		    // cannot play 4 away matches in 5 consecutive matches
		    for(int i=0; i < 14; i++) {
		    	for(int k=0; k < away; k++) {
		    		expr = new GRBLinExpr();
		    		for(int j=0; j < home; j++) {
		    			expr.addTerm(1.0, vars[i][j][k]);
		    			expr.addTerm(1.0, vars[i+1][j][k]);
		    			expr.addTerm(1.0, vars[i+2][j][k]);
		    			expr.addTerm(1.0, vars[i+3][j][k]);
		    			expr.addTerm(1.0, vars[i+4][j][k]);
		    		}
		    		String st = "4AWAYIN5_"+i+"_"+k;
		    		model.addConstr(expr, GRB.LESS_EQUAL, 3, st);
		    	}
		    }
		    model.update();
		    
		    // Each team must have at least 4 weekend games at home
		    for(int j=0; j < home; j++) {
		    	expr = new GRBLinExpr();
		    	for(int i=1; i < slot; i=i+2) {
		    		for(int k=0; k < away; k++) {
		    			expr.addTerm(1.0, vars[i][j][k]);
		    		}
		    	}
		    	String st = "4HOMEWEEKEND_"+j;
		    	model.addConstr(expr, GRB.GREATER_EQUAL, 4, st);
		    }
		    model.update();
		    
		    // Fixed Matches 
		    expr = new GRBLinExpr();
		    expr.addTerm(1.0, vars[5][7][4]);
		    expr.addTerm(1.0, vars[10][2][7]);
		    expr.addTerm(1.0, vars[7][2][0]);
		    expr.addTerm(1.0, vars[13][2][4]);
		    expr.addTerm(1.0, vars[0][3][1]);
		    expr.addTerm(1.0, vars[0][10][11]);
		    expr.addTerm(1.0, vars[0][0][5]);
		    expr.addTerm(1.0, vars[17][7][2]);
		    expr.addTerm(1.0, vars[17][4][0]);
		    model.addConstr(expr, GRB.EQUAL, 9, "FixedMatches");
		    model.update();
		    	    
		    // one of them Fixed1
		    expr = new GRBLinExpr();
		    expr.addTerm(1.0, vars[0][9][6]);
		    expr.addTerm(1.0, vars[0][6][4]);
		    model.addConstr(expr, GRB.EQUAL, 1, "FixedOR1");
		    model.update();
		    
		    // one of them Fixed2
		    expr = new GRBLinExpr();
		    expr.addTerm(1.0, vars[0][6][8]);
		    expr.addTerm(1.0, vars[0][8][9]);
		    model.addConstr(expr, GRB.EQUAL, 1, "FixedOR2");
		    model.update();

		    // one of them Fixed3
		    expr = new GRBLinExpr();
		    expr.addTerm(1.0, vars[17][5][8]);
		    expr.addTerm(1.0, vars[17][8][5]);
		    model.addConstr(expr, GRB.EQUAL, 1, "FixedOR3");
		    model.update();

		    // no home games this slots
		    expr = new GRBLinExpr();
		    for(int k=0; k < away; k++) {
			    expr.addTerm(1.0, vars[6][3][k]);
			    expr.addTerm(1.0, vars[9][3][k]);
			    expr.addTerm(1.0, vars[10][8][k]);
			    expr.addTerm(1.0, vars[11][8][k]);
			    expr.addTerm(1.0, vars[15][11][k]);
		    }
		    model.addConstr(expr, GRB.EQUAL, 0, "NOHOME");
		    model.update();
		    
		    // Single Games Odd Spot
		    expr = new GRBLinExpr();
		    for(int i=0;i<slot;i=i+2){
		    	expr.addTerm(1.0,vars[i][2][11]);
		    }
		    model.addConstr(expr,GRB.EQUAL,1,"SingleGameOdd");
		    model.update();
		    
		    // Single Games Even
		    expr1 = new GRBLinExpr();
		    expr2 = new GRBLinExpr();
		    expr3 = new GRBLinExpr();
		    expr4 = new GRBLinExpr();
		    expr5 = new GRBLinExpr();
		    for(int i =1;i<slot;i=i+2){
		    	expr1.addTerm(1.0,vars[i][2][10]);
		    	expr2.addTerm(1.0,vars[i][8][0]);
		    	expr3.addTerm(1.0,vars[i][8][2]);
		    	expr4.addTerm(1.0,vars[i][0][7]);
		    	expr5.addTerm(1.0,vars[i][5][7]);
		    }
		    model.addConstr(expr1,GRB.EQUAL,1,"SingleGameEven");
		    model.addConstr(expr2,GRB.EQUAL,1,"SingleGameEven2");
		    model.addConstr(expr3,GRB.EQUAL,1,"SingleGameEven3");
		    model.addConstr(expr4,GRB.EQUAL,1,"SingleGameEven4");
		    model.addConstr(expr5,GRB.EQUAL,1,"SingleGameEven5");
		    model.update();
		    
		    
		    for(int k=0; k < 12; k++) {
		    	for(int i=0; i < 5; i++) {
			    	for(int j=0; j < 2; j++) {
			    		binaryCons(model, vars, y, k, matrix2[k][j], k*5+i);
			    	}
		    	}
		    }
		    
		    GRBLinExpr obj = new GRBLinExpr();
		    for(int k=0; k < 60; k++) {
			    obj.addTerm(1, y[k][0]);
			    obj.addTerm(1, y[k][1]);
		    }
		    obj.addTerm(0.07, y[0][0]);
		    obj.addTerm(0.07, y[0][1]);
		    obj.addTerm(0.06, y[1][0]);
		    obj.addTerm(0.06, y[1][1]);
		    obj.addTerm(0.05, y[2][0]);
		    obj.addTerm(0.05, y[2][1]);
		    obj.addTerm(0.04, y[3][0]);
		    obj.addTerm(0.04, y[3][1]);
		    obj.addTerm(0.03, y[4][0]);
		    obj.addTerm(0.03, y[4][1]);
		    obj.addTerm(0.02, y[5][0]);
		    obj.addTerm(0.02, y[5][1]);
		    obj.addTerm(0.01, y[6][0]);
		    obj.addTerm(0.01, y[6][1]);

		    model.setObjective(obj);
		    model.set(GRB.IntAttr.ModelSense, -1);
		    model.optimize();
		    
		    double[][][] x = model.get(GRB.DoubleAttr.X, vars);
			System.out.println();
			for (int i = 0; i < slot; i++) {
		        System.out.println("Week"+(i+1)+" Away 1 2 3 4 5 6 7 8 9 10 11 12 ");
			    for (int j = 0; j < home; j++) {
				    System.out.print("Home Team"+(j+1)+": ");
			    for (int k = 0; k < away; k++) {
			    	System.out.print(x[i][j][k]+" ");
			    	}
			    System.out.println();
			    }
			    System.out.println();
			  }
		    double[][] z = model.get(GRB.DoubleAttr.X, y);
		    for(int i=0; i < 60; i++) {
		    	System.out.print("y"+(i+1)+"   ");
		    }
		    System.out.println();
		    for(int i=0; i < 60; i++) {
		    	System.out.print((z[i][0]+z[i][1])+"  ");
		    }
    
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void binaryCons(GRBModel model, GRBVar[][][] vars, GRBVar[][] y, int j, int k, int z) {
	    // Forcing the binaries
		GRBLinExpr expr;
		GRBLinExpr expr1;
	    expr = new GRBLinExpr();
	    expr1 = new GRBLinExpr();
	    for(int i=0; i < 17; i=i+2) {
	    	expr.addTerm(0.5, vars[i+1][j][k]);
	      	expr.addTerm(0.5, vars[i][k][j]);
	    }
	    for(int i=0; i < 17; i=i+2) {
	    	expr1.addTerm(0.5, vars[i+1][k][j]);
	      	expr1.addTerm(0.5, vars[i][j][k]);
	    }
	    String st = "Binary_"+j+"_"+k;
	    String st2 = "Binary_"+j+"_"+k+"_2";
	    try {
		    model.addConstr(expr, GRB.GREATER_EQUAL, y[z][0], st);
			model.addConstr(expr1, GRB.GREATER_EQUAL, y[z][1], st2);
		    model.update();
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
