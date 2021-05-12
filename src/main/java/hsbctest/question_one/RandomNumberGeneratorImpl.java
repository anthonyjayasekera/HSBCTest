package hsbctest.question_one;

class RandomNumberGeneratorImpl implements RandomNumberGenerator {

    @Override
    public float generate(float min, float max) {
        if (min > max) {
            throw new RuntimeException(String.format("Invalid range min=%s, max=%s", min, max));
        }
       return  min + (max - min) * Double.valueOf(Math.random()).floatValue();
    }
}
