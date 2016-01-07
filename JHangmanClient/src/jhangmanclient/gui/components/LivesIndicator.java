package jhangmanclient.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class LivesIndicator extends JPanel {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Dimension size;

    public LivesIndicator(Dimension size) {
        this.size = size;
    }
    
    public void setSize(Dimension size) {
        this.size = size;
        this.repaint();
    } 
    
    @Override
    public Dimension getPreferredSize() {
        return this.size;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Color backgroundColor = this.getBackground();
        
        

        //draw main ellipse
        g2d.setColor(Color.RED);
        g2d.fillOval(0, 0, this.size.width, this.size.height);
        
        Color surfaceRed = new Color(1.0f,0f,0f,0.5f);
        Color surfaceDark = new Color(.0f,.0f,.0f,0.5f);
        Color[] surfaceColors = {surfaceRed,surfaceDark,surfaceDark,surfaceRed};
        float[] surfaceDistribution = {.2f,.7f,.8f,1f};
        RadialGradientPaint surfacePaint = 
            new RadialGradientPaint(
                this.size.width/2, 
                this.size.height/2, 
                this.size.width/2, 
                surfaceDistribution, 
                surfaceColors,
                java.awt.MultipleGradientPaint.CycleMethod.REPEAT
            );
        g2d.setPaint(surfacePaint);
        g2d.fillOval(0, 0, this.size.width, this.size.height);
        g2d.setColor(backgroundColor);
        int internalW = this.size.width/4;
        int internalH = this.size.height/4;
        g2d.fillOval(internalW,internalH,2*internalW,2*internalH);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawOval(0, 0, this.size.width, this.size.height);
        g2d.drawOval(internalW, internalH, 2*internalW, 2*internalH);
        int intRadius = this.size.width/4;
        int extRadius = this.size.width/2;
        g2d.setColor(backgroundColor);
        int n = 9;
        for (int i=0; i < n; i++) {
            drawSeparator(g2d,new Point(this.size.width/2, this.size.height/2),intRadius, extRadius,i,n); 
        }
    }
    
    private void drawSeparator(Graphics2D g, Point center, int intRadius, int extRadius, int sector, int nSectors) {
        AffineTransform original = g.getTransform();
        g.translate(center.x, center.y);
        double rotation = (2*Math.PI * ((double)sector))/(nSectors);
        System.out.println(rotation);
        g.rotate(rotation);
        g.fillRect(-10, -10, 20, 20);
        double coeff = Math.cos((2*Math.PI)/(nSectors*6));
        double intX = coeff * intRadius;
        double extX = coeff * extRadius;
        coeff = Math.sin((2*Math.PI)/(nSectors*6));
        double intY = coeff * intRadius;
        double extY = coeff * extRadius;
        Point intUpper = new Point((int)intX,(int)-intY);
        Point intLower = new Point((int)intX,(int)intY);
        Point extUpper = new Point((int)extX,(int)-extY);
        Point extLower = new Point((int)extX,(int)extY);
//        highlight(g, intUpper);
//        highlight(g, intLower);
//        highlight(g, extUpper);
//        highlight(g, extLower);
        Polygon p = new Polygon();
        p.addPoint(intUpper.x, intUpper.y);
        p.addPoint(intLower.x,intLower.y);
        p.addPoint(extLower.x * 2,extLower.y * 2);
        p.addPoint(extUpper.x * 2,extUpper.y * 2);
        g.fillPolygon(p);
        g.setTransform(original);
    }
    
    private void highlight(Graphics2D g, Point p) {
        Color c = g.getColor();
        g.setColor(Color.BLACK);
        g.fillRect(p.x-5, p.y-5, 10, 10);
        g.setColor(c);
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        frame.add(new LivesIndicator(new Dimension(200, 200)));
        frame.pack();
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

}