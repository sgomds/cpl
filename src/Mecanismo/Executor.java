package Mecanismo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Executor{

    private final String captureComment = "\\/\\/.*|\\(\\*(?:.|\\s)*\\*\\)";
    private final String captureNumbers = "(?<!\\w)(?:-?\\d+(?:\\.\\d+)?)(?!\\w)";
    private final String captureLiteral = "'(?:[^']|'')*'";
    private final String captureWords   = "\\w+";
    private final String captureCharacters = "(?::=|>=|<=|<>|>|<|=|\\+|\\-|\\*|\\/|[:;,.()\\[\\]{}])";
    private final String captureIdentifier = "^[A-Za-z][A-Za-z0-9_]*$";
    private String capture;

    private BufferedReader reader;
    private ArrayList<String> bufferPrimario;
    private ArrayList<String> bufferSecundario;

    private HashMap<String, Token> tabelaSimbolosPrograma;

    private boolean IsNumber(String valor)
    {
        Pattern pattern = Pattern.compile(this.captureNumbers);
        Matcher matcher = pattern.matcher(valor);
        if (matcher.find() == true){
            return true;
        }
        return false;
    }

    private boolean IsLiteral(String valor)
    {
        Pattern pattern = Pattern.compile(this.captureLiteral);
        Matcher matcher = pattern.matcher(valor);
        if (matcher.find() == true){
            return true;
        }
        return false;
    }

    private boolean IsCharacter(String valor)
    {
        Pattern pattern = Pattern.compile(this.captureCharacters);
        Matcher matcher = pattern.matcher(valor);
        if (matcher.find() == true){
            return true;
        }
        return false;
    }

    private boolean IsIdentifier(String valor)
    {
        Pattern pattern = Pattern.compile(this.captureIdentifier);
        Matcher matcher = pattern.matcher(valor);
        if (matcher.find() == true){
            return true;
        }
        return false;
    }

    public void CarregarArquivo(){
        System.out.println("----------------------------------------");
        System.out.println("##### Carregar Arquivo Pascal #####");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Digite o diretório do arquivo: ");
        String diretorio = scanner.next();
        System.out.print("Digite o nome do arquivo (com extensão .pas): ");
        String nomeArquivo = scanner.next();
        String caminhoCompleto = diretorio + "/" + nomeArquivo;
        this.CarregarBufferPrimario(caminhoCompleto);
        scanner.close();        
    }

    public void CarregarArquivo(String caminhoCompleto){
        System.out.println("----------------------------------------");
        System.out.println("##### Carregar Arquivo Pascal #####");
        this.CarregarBufferPrimario(caminhoCompleto);
    }

    private void CarregarBufferPrimario(String caminhoCompleto){
        this.reader = null;
        try {
            this.reader = new BufferedReader(new FileReader(caminhoCompleto));
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    public void ProcessarBufferPrimario(){
        this.bufferPrimario = new ArrayList<>();
        try {
            String linha;
            while ((linha = this.reader.readLine()) != null) {
                bufferPrimario.add(linha);
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar o arquivo: " + e.getMessage());
                }
            }
        }
    }

    public void ImprimirBufferPrimario(){
        System.out.println("----------------------------------------");
        System.out.println("##### Conteúdo do Buffer primário: #####");
        for (String texto : this.bufferPrimario) {
            System.out.println(texto);
        }
        System.out.println("----------------------------------------");        
    }

    public void ProcessarBufferSecundario()
    {        
        this.capture = captureComment.concat("|")
            .concat(captureNumbers).concat("|")
            .concat(captureLiteral).concat("|")
            .concat(captureWords).concat("|")
            .concat(captureCharacters);

        this.bufferSecundario = new ArrayList<>();

        Pattern pattern = Pattern.compile(this.capture);

        for (String texto : bufferPrimario) {
            Matcher matcher = pattern.matcher(texto);
            while(matcher.find()){
                String lexema = matcher.group();
                if (this.bufferSecundario.contains(lexema) == false){
                    this.bufferSecundario.add(lexema);
                }
            }
        }
        bufferSecundario.removeIf(value -> value.startsWith("//") || value.startsWith("(*"));
    } 
    
    public void ImprimirBufferSecundario(){
        System.out.println("----------------------------------------");
        System.out.println("##### Conteúdo do Buffer secundário: #####");
        for (String texto : this.bufferSecundario) {
            System.out.println(texto);
        }
        System.out.println("----------------------------------------");        
    }

    public void AnalisarMontandoTabelaSimbolos(){
        System.out.println("----------------------------------------");
        System.out.println("##### Analisando e Montando Tabela de Símbolos #####");
    
        // Inicializa a tabela de símbolos do programa
        this.tabelaSimbolosPrograma = new HashMap<>();
    
        // Inicializa a tabela de palavras reservadas da linguagem Pascal
        HashMap<String, String> tabelaSimbolosLinguagem = new HashMap<>();
        String[] palavrasReservadas = {
            "program", "begin", "end", "var", "integer", "real",
            "if", "then", "else", "while", "do", "procedure", "function",
            "for", "to", "repeat", "until", "not", "and", "or", "div", "mod"
        };
        for (String palavra : palavrasReservadas) {
            tabelaSimbolosLinguagem.put(palavra, "PalavraReservada");
        }
    
        // Varre os lexemas do buffer secundário
        for (String lexema : this.bufferSecundario) {
            String tipo;
    
            if (tabelaSimbolosLinguagem.containsKey(lexema.toLowerCase())) {
                tipo = "PalavraReservada";
            } else if (IsNumber(lexema)) {
                tipo = "ConstanteNumerica";
            } else if (IsLiteral(lexema)) {
                tipo = "ConstanteLiteral";
            } else if (IsCharacter(lexema)) {
                tipo = "OperadorOuDelimitador";
            } else if (IsIdentifier(lexema)) {
                tipo = "Identificador";
            } else {
                tipo = "Desconhecido";
            }
    
            if (!tabelaSimbolosPrograma.containsKey(lexema)) {
                Token token = new Token (lexema, tipo);
                tabelaSimbolosPrograma.put(lexema, token);
            }
        }
    }
    

    public void ImprimirTabelaSimbolosPrograma(){

    class Token {
    private final String lexema;
    private final String tipo;

    public Token(String lexema, String tipo) {
        this.lexema = lexema;
        this.tipo = tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return "Lexema: " + lexema + ", Tipo: " + tipo;
    

    }

    }

 }
 }
 
