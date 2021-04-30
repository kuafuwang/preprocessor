package manifold.preprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreprocessorResult {
    public List<Map.Entry<Integer, Integer>> position_infos = new ArrayList<>();
    public List<String> defines = new ArrayList<>();
    public String text;
    public  boolean hasPreprocessorDirectives = true;
}
