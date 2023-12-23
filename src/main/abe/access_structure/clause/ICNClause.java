package access_structure.clause;

import access_structure.AccessStructure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ICNClause implements AccessStructure {
    public List<String> clauses = new ArrayList<>();

    public ICNClause(List<String> clause) {
        List<String> curClause = new ArrayList<>(clause);
        curClause.add("PUB");
        this.clauses = curClause;
    }

    public ICNClause(String clause) {
        String[] attributes = clause.split(" and ");
        this.clauses = new ArrayList<>(Arrays.asList(attributes));
        clauses.add("PUB");
    }
}
