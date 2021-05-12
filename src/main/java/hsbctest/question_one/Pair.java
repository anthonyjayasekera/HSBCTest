package hsbctest.question_one;

public class Pair<LEFT, RIGHT> {

    private LEFT left;
    private RIGHT right;

    private Pair(LEFT left, RIGHT right) {
        this.left = left;
        this.right = right;
    }

    public  static <LEFT, RIGHT> Pair<LEFT, RIGHT> of(LEFT left, RIGHT right) {
        return new Pair<>(left, right);
    }

    public LEFT getLeft() {
        return left;
    }

    public RIGHT getRight() {
        return right;
    }
}
