package accessStructure.tree.node;

public class PolicyAttribute extends TreeNode {
    public String attributeName;
    public Integer ti;
    public Integer tj;
    //x是对象在A中的下标
    public int x;
    public boolean hasSpan;

    public PolicyAttribute(String attribute) {
        if (attribute.matches("[a-z,A-Z,0-9]*:[0-9]*-[0-9]*")) {
            int index1 = attribute.indexOf(":");
            int index2 = attribute.indexOf("-");
            attributeName = attribute.substring(0, index1);
            String attributeLowerLimit = attribute.substring(index1 + 1, index2);
            String attributeUpperLimit = attribute.substring(index2 + 1);

            this.ti = Integer.parseInt(attributeLowerLimit);
            this.tj = Integer.parseInt(attributeUpperLimit);
            hasSpan = true;
        }
        else {
            attributeName = attribute;
            hasSpan = false;
        }
    }

    public int getTi() {
        return ti;
    }

    public int getTj() {
        return tj;
    }

    @Override
    public String getName() {
        return attributeName;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public String toString() {
        return attributeName+":"+ti+"-"+tj;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
        result = prime * result + x;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof PolicyAttribute))
            return false;
        PolicyAttribute other = (PolicyAttribute) obj;
        if (attributeName == null) {
            if (other.attributeName != null)
                return false;
        } else if (!attributeName.equals(other.attributeName))
            return false;
        return x == other.x;
    }
}
