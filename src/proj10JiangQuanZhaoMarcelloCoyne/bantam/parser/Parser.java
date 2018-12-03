/*
 * Authors: Haoyu Song and Dale Skrien
 * Date: Spring and Summer, 2018
 *
 * In the grammar below, the variables are enclosed in angle brackets.
 * The notation "::=" is used instead of "-->" to separate a variable from its rules.
 * The special character "|" is used to separate the rules for each variable.
 * All other symbols in the rules are terminals.
 * EMPTY indicates a rule with an empty right hand side.
 * All other terminal symbols that are in all caps correspond to keywords.
 */
package proj10JiangQuanZhaoMarcelloCoyne.bantam.parser;

import proj10JiangQuanZhaoMarcelloCoyne.Scanner.*;
import proj10JiangQuanZhaoMarcelloCoyne.bantam.ast.*;

import static proj10JiangQuanZhaoMarcelloCoyne.Scanner.Token.Kind.COMMA;
import static proj10JiangQuanZhaoMarcelloCoyne.Scanner.Token.Kind.DOT;
import static proj10JiangQuanZhaoMarcelloCoyne.Scanner.Token.Kind.EOF;

/**
 * This class constructs an AST from a legal Bantam Java program.  If the
 * program is illegal, then one or more error messages are displayed.
 */
public class Parser
{
    // instance variables
    private Scanner scanner;
    private Token currentToken; // the lookahead token
    private ErrorHandler errorHandler;

    // constructor
    public Parser(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }


    /**
     * parse the given file and return the root node of the AST
     * @param filename The name of the Bantam Java file to be parsed
     * @return The Program node forming the root of the AST generated by the parser
     */
    public Program parse(String filename) {

    }


    /* 
     * <Program> ::= <Class> | <Class> <Program>
     */
    private Program parseProgram() {
        int position = currentToken.position;
        ClassList classList = new ClassList(position);

        while (currentToken.kind != EOF) {
            Class_ aClass = parseClass();
            classList.addElement(aClass);
        }

        return new Program(position, classList);
    }


    /*
	 * <Class> ::= CLASS <Identifier> <ExtendsClause> { <MemberList> }
     * <ExtendsClause> ::= EXTENDS <Identifier> | EMPTY
     * <MemberList> ::= EMPTY | <Member> <MemberList>
     */
    private Class_ parseClass() { }


    /* Fields and Methods
     * <Member> ::= <Field> | <Method>
     * <Method> ::= <Type> <Identifier> ( <Parameters> ) <Block>
     * <Field> ::= <Type> <Identifier> <InitialValue> ;
     * <InitialValue> ::= EMPTY | = <Expression>
     */
     private Member parseMember() { }


    //-----------------------------------

    /* Statements
     *  <Stmt> ::= <WhileStmt> | <ReturnStmt> | <BreakStmt> | <DeclStmt>
     *              | <ExpressionStmt> | <ForStmt> | <BlockStmt> | <IfStmt>
     */
     private Stmt parseStatement() {
            Stmt stmt;

            switch (currentToken.kind) {
                case IF:
                    stmt = parseIf();
                    break;
                case LCURLY:
                    stmt = parseBlock();
                    break;
                case VAR:
                    stmt = parseDeclStmt();
                    break;
                case RETURN:
                    stmt = parseReturn();
                    break;
                case FOR:
                    stmt = parseFor();
                    break;
                case WHILE:
                    stmt = parseWhile();
                    break;
                case BREAK:
                    stmt = parseBreak();
                    break;
                default:
                    stmt = parseExpressionStmt();
            }

            return stmt;
    }


    /*
     * <WhileStmt> ::= WHILE ( <Expression> ) <Stmt>
     */
    private Stmt parseWhile() {
        Stmt stmt;
        int position = this.currentToken.position;

        this.currentToken = scanner.scan();     // left paren
        Expr preExpr = parseExpression();
        this.currentToken = scanner.scan();     // right paren
        Stmt bodyStmt = parseStatement();

        stmt = new WhileStmt(position, preExpr, bodyStmt);
        return stmt;

    }


    /*
     * <ReturnStmt> ::= RETURN <Expression> ; | RETURN ;
     */
	private Stmt parseReturn() {
	    Stmt stmt;
	    int position = this.currentToken.position;

	    this.currentToken = this.scanner.scan();

	    if (this.currentToken.spelling.equals(";")){
	        stmt = new ReturnStmt(position, null);
        }
        else{
            Expr returnExpr = parseExpression();
            stmt = new ReturnStmt(position, returnExpr);
        }

        return stmt;
    }


    /*
	 * BreakStmt> ::= BREAK ;
     */
	private Stmt parseBreak() {
	    int position = this.currentToken.position;
	    return new BreakStmt(position);
    }


    /*
	 * <ExpressionStmt> ::= <Expression> ;
     */
	private ExprStmt parseExpressionStmt() {
	    int position = this.currentToken.position;
	    Expr expr = parseExpression();
	    return new ExprStmt(position, expr);

    }


    /*
	 * <DeclStmt> ::= VAR <Identifier> = <Expression> ;
     * every local variable must be initialized
     */
	private Stmt parseDeclStmt() {
	    int position = this.currentToken.position;
	    String name = this.currentToken.spelling;
	    this.currentToken = scanner.scan();     // "="
        Expr expr = parseExpression();
	    return new DeclStmt(position, name, expr);

    }


    /*
	 * <ForStmt> ::= FOR ( <Start> ; <Terminate> ; <Increment> ) <STMT>
     * <Start>     ::= EMPTY | <Expression>
     * <Terminate> ::= EMPTY | <Expression>
     * <Increment> ::= EMPTY | <Expression>
     */
	private Stmt parseFor() { }


    /*
	 * <BlockStmt> ::= { <Body> }
     * <Body> ::= EMPTY | <Stmt> <Body>
     */
	private Stmt parseBlock() { }


    /*
	 * <IfStmt> ::= IF ( <Expr> ) <Stmt> | IF ( <Expr> ) <Stmt> ELSE <Stmt>
     */
	private Stmt parseIf() { }


    //-----------------------------------------
    // Expressions
    //Here we introduce the precedence to operations

    /*
	 * <Expression> ::= <LogicalOrExpr> <OptionalAssignment>
     * <OptionalAssignment> ::= EMPTY | = <Expression>
     */
	private Expr parseExpression() { }


    /*
	 * <LogicalOR> ::= <logicalAND> <LogicalORRest>
     * <LogicalORRest> ::= EMPTY |  || <LogicalAND> <LogicalORRest>
     */
	private Expr parseOrExpr() {
        int position = currentToken.position;

        Expr left = parseAndExpr();
        while (this.currentToken.spelling.equals("||")) {
            this.currentToken = scanner.scan();
            Expr right = parseAndExpr();
            left = new BinaryLogicOrExpr(position, left, right);
        }

        return left;
	}


    /*
	 * <LogicalAND> ::= <ComparisonExpr> <LogicalANDRest>
     * <LogicalANDRest> ::= EMPTY |  && <ComparisonExpr> <LogicalANDRest>
     */
	private Expr parseAndExpr() { }


    /*
	 * <ComparisonExpr> ::= <RelationalExpr> <equalOrNotEqual> <RelationalExpr> |
     *                     <RelationalExpr>
     * <equalOrNotEqual> ::=  == | !=
     */
	private Expr parseEqualityExpr() { }


    /*
	 * <RelationalExpr> ::=<AddExpr> | <AddExpr> <ComparisonOp> <AddExpr>
     * <ComparisonOp> ::=  < | > | <= | >= | INSTANCEOF
     */
	private Expr parseRelationalExpr() { }


    /*
	 * <AddExpr>::＝ <MultExpr> <MoreMultExpr>
     * <MoreMultExpr> ::= EMPTY | + <MultExpr> <MoreMultExpr> | - <MultExpr> <MoreMultExpr>
     */
	private Expr parseAddExpr() { }


    /*
	 * <MultiExpr> ::= <NewCastOrUnary> <MoreNCU>
     * <MoreNCU> ::= * <NewCastOrUnary> <MoreNCU> |
     *               / <NewCastOrUnary> <MoreNCU> |
     *               % <NewCastOrUnary> <MoreNCU> |
     *               EMPTY
     */
	private Expr parseMultExpr() { }

    /*
	 * <NewCastOrUnary> ::= < NewExpression> | <CastExpression> | <UnaryPrefix>
     */
	private Expr parseNewCastOrUnary() { }


    /*
	 * <NewExpression> ::= NEW <Identifier> ( ) | NEW <Identifier> [ <Expression> ]
     */
	private Expr parseNew() { }


    /*
	 * <CastExpression> ::= CAST ( <Type> , <Expression> )
     */
	private Expr parseCast() { }


    /*
	 * <UnaryPrefix> ::= <PrefixOp> <UnaryPrefix> | <UnaryPostfix>
     * <PrefixOp> ::= - | ! | ++ | --
     */
	private Expr parseUnaryPrefix() { }


    /*
	 * <UnaryPostfix> ::= <Primary> <PostfixOp>
     * <PostfixOp> ::= ++ | -- | EMPTY
     */
	private Expr parseUnaryPostfix() { }


    /*
	 * <Primary> ::= ( <Expression> ) | <IntegerConst> | <BooleanConst> |
     *                               <StringConst> | <VarExpr> | <DispatchExpr>
     * <VarExpr> ::= <VarExprPrefix> <Identifier> <VarExprSuffix>
     * <VarExprPrefix> ::= SUPER . | THIS . | EMPTY
     * <VarExprSuffix> ::= [ <Expr> ] | EMPTY
     * <DispatchExpr> ::= <DispatchExprPrefix> <Identifier> ( <Arguments> )
     * <DispatchExprPrefix> ::= <Primary> . | EMPTY
     */
	private Expr parsePrimary() {
        int position = currentToken.position;
        Expr expr;
        switch (currentToken.kind){
            case INTCONST:
                expr = parseIntConst();
                break;
            case STRCONST:
                expr = parseStringConst();
                break;
            case BOOLEAN:
                expr = parseBoolean();
                break;
            default:
                Expr ref = null;
                if(currentToken.spelling.equals("this")||currentToken.spelling.equals("super")){
                    ref = new VarExpr(position, null, currentToken.spelling);
                    this.currentToken=scanner.scan(); //"this." or "super."
                    if(!currentToken.spelling.equals(".")){ //"this" or "super"
                        expr = ref;
                        return expr;
                    }
                }
                this.currentToken = scanner.scan();
                String name = parseIdentifier();
                if (!currentToken.spelling.equals("(")) {//not dispatch
                    if (!currentToken.spelling.equals("[")) {//not array member
                        expr = new VarExpr(position, ref, name);
                    } else {//array member
                        this.currentToken = scanner.scan();
                        Expr index = parseExpression();
                        expr = new ArrayExpr(position, ref, name, index);
                        this.currentToken = scanner.scan();
                        if(!this.currentToken.spelling.equals("]")){
                            //TODO: Throw compilation error
                        }
                    }

                } else {//dispatch
                    this.currentToken = scanner.scan();
                    ExprList paraList = new ExprList(position);
                    while(this.currentToken.spelling.equals(",")){//parse parameters
                        this.currentToken = scanner.scan();
                        paraList.addElement(parseExpression());
                    }
                    if(!this.currentToken.spelling.equals((")"))){
                        //TODO: Throw compilation error
                    }
                    expr = new DispatchExpr(position, ref, name, paraList);
                }
                return expr;

        }
    }


    /*
	 * <Arguments> ::= EMPTY | <Expression> <MoreArgs>
     * <MoreArgs>  ::= EMPTY | , <Expression> <MoreArgs>
     */
	private ExprList parseArguments() { }


    /*
	 * <Parameters>  ::= EMPTY | <Formal> <MoreFormals>
     * <MoreFormals> ::= EMPTY | , <Formal> <MoreFormals
     */
	private FormalList parseParameters() { }


    /*
	 * <Formal> ::= <Type> <Identifier>
     */
	private Formal parseFormal() { }


    /*
	 * <Type> ::= <Identifier> <Brackets>
     * <Brackets> ::= EMPTY | [ ]
     */
     */
	private String parseType() { }


    //----------------------------------------
    //Terminals

	private String parseOperator() { }


    private String parseIdentifier() { }


    private ConstStringExpr parseStringConst() { }


    private ConstIntExpr parseIntConst() { }


    private ConstBooleanExpr parseBoolean() { }

}

