import java.util.Stack;

public class Main {

    public static int maxHistogramArea(int[] heights) {
        Stack<Integer> stack = new Stack<>();
        int maxArea = 0;
        int i = 0;
        int n = heights.length;

        while (i < n) {
            if (stack.isEmpty() || heights[stack.peek()] <= heights[i]) {
                stack.push(i++);
            } else {
                int tp = stack.pop();
                //int v=stack.isEmpty() ? -1 : stack.peek();
                int area = heights[tp] * (stack.isEmpty() ? i : i - stack.peek() - 1);
                maxArea = Math.max(maxArea, area);
            }
        }

        while (!stack.isEmpty()) {
            int tp = stack.pop();
            int area = heights[tp] * (stack.isEmpty() ? i : i - stack.peek() - 1);
            maxArea = Math.max(maxArea, area);
        }

        return maxArea;
    }

    public static void main(String[] args) {
        int[] heights = {6,4,4,2,1};
        System.out.println("Maximum area of histogram: " + maxHistogramArea(heights));
    }
}
