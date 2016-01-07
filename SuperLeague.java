
import gurobi.*;

public class SuperLeague {
	public static void main(String args[]) {
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
			int week = 34;
			int home = 18;
			int away = 18;

		    GRBVar[][][] vars = new GRBVar[week][home][away];
		    
		    // Initializing variables
		    for (int i=0; i < week; i++) {
		    	for(int j=0; j < home; j++) {
		    		for(int k=0; k < away; k++) {
		    			String st = "G_" + String.valueOf(i) + "_" + 
		    		String.valueOf(j) + "_" + String.valueOf(k);
		    		vars[i][j][k] = model.addVar(0, 1.0, 3, GRB.BINARY, st);
		    		}
		    	}
		    }
		    model.update();
			
		    GRBLinExpr expr;
		    
		    // Every team can play 1 matches each week
		    for(int i=0; i < week/2; i++) {
		    	for(int j=0; j< home; j++) {
				    expr = new GRBLinExpr();
				    for(int k=0; k < away; k++) {
				    	expr.addTerm(1.0, vars[i][j][k]);
				    	expr.addTerm(1.0, vars[i][k][j]);
				    }
				    String st = "V_" + String.valueOf(i) + "_" + String.valueOf(j) + "_k";
				    model.addConstr(expr, GRB.EQUAL, 1.0, st);
		    	}
		    }
		    model.update();
		    
		    // Match the teams twice in a year 
		    for(int k=0; k < away; k++) {
		    	for(int j=0; j < away; j++) {
				    expr = new GRBLinExpr();
				    for(int i=0; i < week; i++) {
				    	if ( k == j) {
				    		
				    	}
				    	else {
					    	expr.addTerm(1.0, vars[i][k][j]);
				    	}
				    }
				    String st = "MATCH_"+k+"_"+j;
				    model.addConstr(expr, GRB.LESS_EQUAL, 1, st);
		    	}
		    }
		    model.update();
		    
		    // Genclerbirligi - Osmanli
		    for(int i=0; i< week/2; i++) {
		    	expr = new GRBLinExpr();
		    	for(int k=0; k < away; k++) {
		    		expr.addTerm(1.0, vars[i][9][k]);
		    		expr.addTerm(1.0, vars[i][15][k]);
		    	}
		    	String st = i+"_genclerosmanli";
		    	model.addConstr(expr, GRB.LESS_EQUAL, 1, st);
		    }
		    model.update();

		    // Can't match with itself!
		    expr = new GRBLinExpr();
		    for(int i=0; i < week/2; i++) {
		    	for(int j=0; j < home; j++) {
		    		expr.addTerm(1.0, vars[i][j][j]);
		    	}
		    }
		    model.addConstr(expr, GRB.EQUAL, 0, "cantmatchitself");
		    model.update();
		    
		    // Besiktas - Basaksehir 
		    for(int i=0; i < week/2; i++) {
		    	expr = new GRBLinExpr();
		    	for(int k=0; k < away; k++) {
		    		expr.addTerm(1.0, vars[i][2][k]);
		    		expr.addTerm(1.0, vars[i][13][k]);
		    	}
		    String st = i+"_besiktas_basaksehir";
		    model.addConstr(expr, GRB.LESS_EQUAL, 1, st);
		    }
		    model.update();
		    
		    // FB - GS
		    expr = new GRBLinExpr();
		    expr.addTerm(1.0, vars[0][6][7]);
		    expr.addTerm(1.0, vars[0][7][6]);
		    expr.addTerm(1.0, vars[1][6][7]);
		    expr.addTerm(1.0, vars[1][7][6]);
		    expr.addTerm(1.0, vars[16][6][7]);
		    expr.addTerm(1.0, vars[16][7][6]);
		    model.addConstr(expr, GRB.EQUAL, 0, "FB-GS");
		    model.update();
		    
		    // Kayserispor
		    expr = new GRBLinExpr();
		    expr.addTerm(1.0, vars[8][11][3]);
		    expr.addTerm(1.0, vars[8][11][10]);
		    expr.addTerm(1.0, vars[8][3][11]);
		    expr.addTerm(1.0, vars[8][10][11]);
		    model.addConstr(expr, GRB.EQUAL, 1, "Kayserispor");
		    model.update();
		    
		    // Gaziantep - Basaksehir
		    for(int i=0; i < week/2; i++) {
		    	expr = new GRBLinExpr();
		    	expr.addTerm(1.0, vars[i][8][13]);
		    	expr.addTerm(1.0, vars[i][13][8]);
		    	expr.addTerm(-1.0, vars[i][4][14]);
		    	expr.addTerm(-1.0, vars[i][14][4]);
		    	String st = i+"_gaziantep_basaksehir";
		    	model.addConstr(expr, GRB.LESS_EQUAL, 0, st);
		    }
		    model.update();
		    
		    // Konyaspor - Akhisar
		    expr = new GRBLinExpr();
		    expr.addTerm(1.0, vars[14][0][16]);
		    for(int k=0; k < away; k++) {
		    	expr.addTerm(1.0, vars[11][16][k]);
		    }
		    model.addConstr(expr, GRB.GREATER_EQUAL, 1, "konyaspor");
		    model.update();
		    
		    // Week-6 Constraint
		    expr = new GRBLinExpr();
		    expr.addTerm(1.0, vars[5][7][6]);
		    expr.addTerm(1.0, vars[5][6][7]);
		    expr.addTerm(1.0, vars[5][7][12]);
		    expr.addTerm(1.0, vars[5][12][7]);
		    expr.addTerm(1.0, vars[5][7][5]);
		    expr.addTerm(1.0, vars[5][5][7]);
		    expr.addTerm(1.0, vars[5][17][6]);
		    expr.addTerm(1.0, vars[5][6][17]);
		    expr.addTerm(1.0, vars[5][17][12]);
		    expr.addTerm(1.0, vars[5][12][17]);
		    expr.addTerm(1.0, vars[5][17][5]);
		    expr.addTerm(1.0, vars[5][5][17]);
		    expr.addTerm(1.0, vars[5][2][6]);
		    expr.addTerm(1.0, vars[5][6][2]);
		    expr.addTerm(1.0, vars[5][2][12]);
		    expr.addTerm(1.0, vars[5][12][2]);
		    expr.addTerm(1.0, vars[5][2][5]);
		    expr.addTerm(1.0, vars[5][5][2]);
		    model.addConstr(expr, GRB.GREATER_EQUAL, 2, "week-6");
		    model.update();
		    
		    // First Half - Second Half Relation for each week
		    for(int i=0; i < week/2; i++) {
		    	for(int j=0; j < home; j++) {
		    		for(int k=0; k < away; k++) {
		    			expr = new GRBLinExpr();
		    			expr.addTerm(1.0, vars[i][j][k]);
		    			expr.addTerm(-1.0, vars[i+17][k][j]);
		    			String st = "FS_"+i+"_"+j+"_"+k;
		    			model.addConstr(expr, GRB.EQUAL, 0, st);
		    		}
		    	}
		    }
		    model.update();

		    model.optimize();
		    
		    double[][][] x = model.get(GRB.DoubleAttr.X, vars);
			System.out.println();
			for (int i = 0; i < week; i++) {
		        System.out.println("Week"+(i+1)+" Away 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18");
			    for (int j = 0; j < home; j++) {
				    System.out.print("Home Team"+(j+1)+": ");
			    for (int k = 0; k < away; k++) {
			    	System.out.print(x[i][j][k]+" ");
			    	}
			    System.out.println();
			    }
			    System.out.println();
			  }
			
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
