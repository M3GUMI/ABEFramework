package access_structure.clause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TongClause {
    public List<String> clauses = new ArrayList<>();

    public TongClause(List<String> clause) {
        List<String> curClause = new ArrayList<>(clause);
        this.clauses = curClause;
    }

    public TongClause(String clause) {
        String[] attributes = clause.split(" and ");
        this.clauses = new ArrayList<>(Arrays.asList(attributes));
    }
}
