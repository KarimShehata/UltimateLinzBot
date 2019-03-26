package ampullen;

import javafx.util.Pair;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ampullen.AsciiTable.ColumnDefinition.Column;

public class AsciiTable <T> {

    Message tableMessage;
    ColumnDefinition<T> columns;

    List<List<String>> data = new ArrayList<>();

    public AsciiTable(Message tableMessage, ColumnDefinition columns){
        this.tableMessage = tableMessage;
        this.columns = columns;
    }

    public AsciiTable data(String infoData, T rowData){

        if(columns.columns.stream().filter(x -> x.pk || x.infoColumn).count() == 1){

            List<T> multiplied = new ArrayList<>();
            for(int i = 0 ; i < columns.columns.stream().filter(x -> x.predicate != null).count() ; i++){

                multiplied.add(rowData);

            }
            return data(Arrays.asList(infoData), multiplied);

        }else{
            throw new IllegalStateException("Method call for more than one infocolumn not available");
        }

    }

    public AsciiTable data(List<String> infoData, List<T> rowData){

        //Make lists mutable
        infoData = new ArrayList<>(infoData);
        rowData = new ArrayList<>(rowData);

        List<String> row = new ArrayList<>();

        //Create new row
        for(ColumnDefinition.Column<T> column : columns.columns){

            if(column.infoColumn){

                row.add(infoData.get(0));
                infoData.remove(0);

            }else if(column.predicate != null){

                row.add(column.predicate.test(rowData.get(0)) ? "X" : "");
                rowData.remove(0);

            }else{
                System.out.println("Predicate not set for non-info column");
            }

        }

        //Remove old and set new
        int pkIndex = -1;
        for(int i = 0 ; i < columns.columns.size() && pkIndex == -1 ; i++){

            if(columns.columns.get(i).pk){
                pkIndex = i;
            }

        }

        boolean set = false;
        for(int i = 0 ; i < data.size() ; i++){

            if(data.get(i).get(pkIndex).equals(row.get(pkIndex))){

                data.set(i, row);
                set = true;

            }
        }
        if(!set){
            data.add(row);
        }

        return this;
    }

    public String renderAscii(){

        // create table header
        StringBuilder header = new StringBuilder();

        List<Integer> paddings = columns.columns.stream().map(this::calculateColumnPadding).collect(Collectors.toList());

        for (int i = 0 ; i < columns.columns.size() ; i++){

            Column<T> column = columns.columns.get(i);

            header.append(" ").append(Utilities.padRight(column.columnname, paddings.get(i))).append(" |");

        }

        header = new StringBuilder(header.substring(0, header.length() - 2) + "\n");

        // create separator
        StringBuilder separator = new StringBuilder();

        for (char ignored : header.toString().toCharArray()) {
            separator.append("=");
        }

        separator.append("\n");

        // create entries
        StringBuilder entries = new StringBuilder();

        Map<Column<T>, Integer> sums = columns.columns.stream().collect(Collectors.toMap(Function.identity(), x -> 0));

        for (List<String> d : data) {

            for(int i = 0 ; i < columns.columns.size() ; i++) {

                entries.append(" ").append(Utilities.padRight(d.get(i), paddings.get(i))).append(" |");

                Column<T> column = columns.columns.get(i);

                if (!column.infoColumn && d.get(i).equals("X")) {

                    sums.put(column, sums.get(column) + 1);

                }

            }

            entries = new StringBuilder(entries.substring(0, entries.length() - 1)); //TODO Replace with entries.delete
            entries.append("\n");
        }

        // create summary

        StringBuilder summary = new StringBuilder(" ");

        for(int i = 0 ; i < columns.columns.size() ; i++){

            Column<T> column = columns.columns.get(i);

            if(column.pk){

                summary.append(Utilities.padRight(data.size() + "", paddings.get(i))).append(" |");

            }else if(!column.infoColumn){

                summary.append(" ").append(Utilities.padRight(sums.get(column) + "", paddings.get(i))).append(" |");

            }

        }

        summary = new StringBuilder(summary.substring(0, summary.length() - 1));

        return  header +
                separator.toString() +
                entries +
                separator +
                summary;
    }

    private int calculateColumnPadding(ColumnDefinition.Column<T> column) {

        int index = columns.columns.indexOf(column);

        return Stream.concat(data.stream()
                .map(x -> x.get(index).length()),
                Stream.of(column.columnname.length()))
                .mapToInt(x -> x)
                .max().orElse(1);
    }

    public static class ColumnDefinition<T>{

        private List<Column<T>> columns;

        public ColumnDefinition(){
//            this.columns = columns;
            columns = new ArrayList<>();
        }

        public ColumnDefinition<T> addInfoColumn(String name){
            columns.add(new Column<>(name, null, true, false));
            return this;
        }

        public ColumnDefinition<T> addPrimaryColumn(String name){
            if(columns.stream().anyMatch(x -> x.pk)){
                throw new IllegalStateException("Primary key already defined");
            }

            columns.add(new Column<>(name, null, true, true));
            return this;
        }

        public ColumnDefinition<T> addColumn(String name, Predicate<T> predicate){
            columns.add(new Column<>(name, predicate, false, false));
            return this;
        }

        static class Column<T>{
            String columnname;
            Predicate<T> predicate;
            boolean infoColumn;
            boolean pk;

            public Column(String columnname, Predicate<T> predicate, boolean infoColumn, boolean pk) {
                this.columnname = columnname;
                this.predicate = predicate;
                this.infoColumn = infoColumn;
                this.pk = pk;
            }
        }

    }

}
