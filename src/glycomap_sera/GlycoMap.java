package glycomap_sera;//GEN-LINE:initComponents

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.universe.SimpleUniverse;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.*;
import javax.swing.ToolTipManager;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.text.TextBlock;
import org.jfree.ui.RectangleEdge;

public class GlycoMap extends Applet implements MouseListener, MouseMotionListener
{
    public GlycoMap()
    {
        try
        {
            init();
            
            canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
            canvas.addMouseListener(this);
            canvas.addMouseMotionListener(this);

            panelcen = new Panel(new GridBagLayout());
            paneltop = new Panel(new GridLayout(2, 1));
            paneltopu = new Panel();
            paneltopb = new Panel();
            panelbot = new Panel();
            panelsid = new Panel(new GridLayout(2, 1, 0, 200));
            
            b2d = new Button("2D Map");
            b3d = new Button("3D Map");
            bhm = new Button("Heat Map");
            sbcid = new SpecButton("CID Spectra");
            sbhcd = new SpecButton("HCD Spectra");
            
            panelcen.setBackground(Color.BLACK);
            paneltop.setBackground(Color.BLACK);
            panelbot.setBackground(Color.BLACK);
            panelsid.setBackground(Color.BLACK);
            
            paneltop.setPreferredSize(new Dimension(getWidth(), 80));
            panelsid.setPreferredSize(new Dimension(190, getHeight()));
            paneltop.add(paneltopu);
            paneltop.add(paneltopb);
            panelsid.add(sbcid);
            panelsid.add(sbhcd);
            panelbot.add(b3d);
            panelbot.add(b2d);
            panelbot.add(bhm);

            add(paneltop, BorderLayout.NORTH);
            add(panelbot, BorderLayout.SOUTH);
            add(panelsid, BorderLayout.EAST);
            
            b2d.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    extractCrd2D();
                }
            });
            b3d.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    extractCrd3D();
                }
            });
            bhm.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                    try {
                        extractCrdHM();
                    } catch (IOException ex) {
                        Logger.getLogger(GlycoMap.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            

            crd = new Coordinates();
            mass = crd.getMass();
            net = crd.getNET();
            peptide = crd.getPeptide();
            glycan = crd.getGlycan();
            proteinid = crd.getProteinID();
            site = crd.getSite();
            cidlen = crd.getCIDLen();
            cidspec = crd.getCIDSpec();
            hcdlen = crd.getHCDLen();
            hcdspec = crd.getHCDSpec();
            intca = crd.getIntCa();
            intco = crd.getIntCo();
            nzca = crd.getNonZeroCa();
            nzco = crd.getNonZeroCo();
            charge = crd.getCharge();
            t1 = null;
            t2 = null;
            paint = new Paint[2 * mass.length];

            xmax = 0.0;
            for(int i = 0; i < net.length; i++)
            {
                if(net[i] > xmax)
                {
                    xmax = net[i];
                }
            }
            xmax = ((double)Math.round(xmax * 16) / 10) - 0.7;
            zmax = 0.0;
            for(int i = 0; i < mass.length; i++)
            {
                if(mass[i] > zmax)
                {
                    zmax = mass[i];
                }
            }
            zmax = ((double)Math.round(zmax / 500) / 10) + 0.1;
            
            double start = -0.7;
            double end = xmax - 0.1;
            tickposx = new Double[(int)(((end - start) * 10) + 1)];
            for(int i = 0; i < tickposx.length; i++)
            {
                tickposx[i] = start;
                start += 0.1;
            }
            tickposy = new Double[]{0.05, 0.1, 0.15, 0.20, 0.25};
            start = (zmax / -2) + 0.1;
            end = (zmax / 2) - 0.1;
            tickposz = new Double[(int)(((end - start) * 10) + 1)];
            for(int i = 0; i < tickposz.length; i++)
            {
                tickposz[i] = start;
                start += 0.1;
            }
            df = new DecimalFormat("0.00");
            
            obj = (2 * mass.length) + tickposx.length + tickposy.length + tickposz.length + 7;
            t3 = new Transform3D[obj];
            tg = new TransformGroup[obj];
            vec = new Vector3d[obj];

            bs = new BoundingSphere(new Point3d(), Double.MAX_VALUE);
            scene = new BranchGroup();
            extractCrd3D();
            createSceneGraph();
            
            for(int i = 0, j = 0; i < mass.length; i++, j += 2)
            {
                if(intco[i] < 1.0)
                {
                    intco[i] = 1.0;
                }
                if(intca[i] < 1.0)
                {
                    intca[i] = 1.0;
                }
                intco[i] = Math.log(intco[i]);
                intca[i] = Math.log(intca[i]);
                if(intco[i] != 0.0)
                {
                    intco[i] -= 8.0;
                }
                if(intca[i] != 0.0)
                {
                    intca[i] -= 8.0;
                }   
                intco[i] *= 25.0;
                intca[i] *= 25.0;
                
                int r = (int)intco[i].doubleValue();
                paint[j] = new Color(r, 0, 0);
                r = (int)intca[i].doubleValue();
                paint[j + 1] = new Color(r, 0, 0);
            }
            
            su = new SimpleUniverse(canvas);
            su.getViewingPlatform().setNominalViewingTransform();
            su.addBranchGraph(scene);

            pc = new PickCanvas(canvas, scene);
            ob = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
            ob.setSchedulingBounds(bs);
            ob.setRotFactors(1, 1);
            ob.setRotationCenter(new Point3d());
            ob.setRotateEnable(true);
            ob.setTransFactors(0.2, 0.2);
            ob.setTranslateEnable(true);
            ob.setZoomFactor(0.2);
            ob.setZoomEnable(true);
            su.getViewingPlatform().setViewPlatformBehavior(ob);
            ob.setViewingPlatform(su.getViewingPlatform());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public final void extractCrd3D()
    {
        remove(panelcen);
        add(canvas, BorderLayout.CENTER);
        
        int i;
        for(i = 0; i < mass.length; i++)
        {
            if(nzca[i] != 0)
            {
                intca[i] /= nzca[i];
            }
            
            if(nzco[i] != 0)
            {
                intco[i] /= nzco[i];
            }
            
            heightca = intca[i] / (8 * (intca[i] + intco[i]));
            heightco = 0.125 - heightca;
            
            vec[i] = new Vector3d((net[i] * 1.6) - 0.8, heightca, (mass[i] / 5000) - (zmax / 2));

            vec[i + mass.length] = new Vector3d((net[i] * 1.6) - 0.8, (0.125 + heightca), (mass[i] / 5000) - (zmax / 2));

            t3[i] = new Transform3D();
            t3[i].setTranslation(vec[i]);

            t3[i + mass.length] = new Transform3D();
            t3[i + mass.length].setTranslation(vec[i + mass.length]);

            tg[i] = new TransformGroup();
            tg[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tg[i].setTransform(t3[i]);
            tg[i].addChild(new Box(0.005f, (float)heightca, 0.005f, barAppearance(255, 255, 102, 0)));
            tg[i].addChild(new Box(0.005f, (float)heightca, 0.005f, barAppearance(0, 102, 0, 1)));

            tg[i + mass.length] = new TransformGroup();
            tg[i + mass.length].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tg[i + mass.length].setTransform(t3[i + mass.length]);
            tg[i + mass.length].addChild(new Box(0.005f, (float)heightco, 0.005f, barAppearance(255, 122, 0, 0)));
            tg[i + mass.length].addChild(new Box(0.005f, (float)heightco, 0.005f, barAppearance(255, 0, 0, 1)));
        }
        
        vec[i + mass.length] = new Vector3d((xmax - 0.8) / 2, 0, 0);
        t3[i + mass.length] = new Transform3D();
        t3[i + mass.length].setTranslation(vec[i + mass.length]);
        tg[i + mass.length] = new TransformGroup();
        tg[i + mass.length].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg[i + mass.length].setTransform(t3[i + mass.length]);
        tg[i + mass.length].addChild(new Box((float)((xmax + 0.8) / 2), 0, (float)(zmax / 2), axisAppearance(229, 255, 204, 0)));
        tg[i + mass.length].addChild(new Box((float)((xmax + 0.8) / 2), 0, (float)(zmax / 2), axisAppearance(80, 125, 42, 1)));

        vec[i + mass.length + 1] = new Vector3d(-0.8, 0.2, 0);
        t3[i + mass.length + 1] = new Transform3D();
        t3[i + mass.length + 1].setTranslation(vec[i + mass.length + 1]);
        tg[i + mass.length + 1] = new TransformGroup();
        tg[i + mass.length + 1].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg[i + mass.length + 1].setTransform(t3[i + mass.length + 1]);
        tg[i + mass.length + 1].addChild(new Box(0, 0.2f, (float)(zmax / 2), axisAppearance(229, 255, 204, 0)));
        tg[i + mass.length + 1].addChild(new Box(0, 0.2f, (float)(zmax / 2), axisAppearance(80, 125, 42, 1)));

        vec[i + mass.length + 2] = new Vector3d((xmax - 0.8) / 2, 0.2, (zmax / -2));
        t3[i + mass.length + 2] = new Transform3D();
        t3[i + mass.length + 2].setTranslation(vec[i + mass.length + 2]);
        tg[i + mass.length + 2] = new TransformGroup();
        tg[i + mass.length + 2].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg[i + mass.length + 2].setTransform(t3[i + mass.length + 2]);
        tg[i + mass.length + 2].addChild(new Box((float)((xmax + 0.8) / 2), 0.2f, 0, axisAppearance(229, 255, 204, 0)));
        tg[i + mass.length + 2].addChild(new Box((float)((xmax + 0.8) / 2), 0.2f, 0, axisAppearance(80, 125, 42, 1)));
        
        int j = i + mass.length + 3;
        for(int k = 0; k < tickposx.length; j++, k++)
        {
            vec[j] = new Vector3d(tickposx[k], -0.02, (zmax / 2));
            t3[j] = new Transform3D();
            t3[j].setTranslation(vec[j]);
            t3[j].setScale(0.02);
            tg[j] = new TransformGroup();
            tg[j].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tg[j].setTransform(t3[j]);
            double temp = (tickposx[k] + 0.8) / 1.6;
            tg[j].addChild(new Shape3D(new Text3D(new Font3D(new Font("Book Antiqua", Font.BOLD, 1), new FontExtrusion()), df.format(temp), new Point3f()), textAppearance()));
        }
        
        for(int k = 0; k < tickposy.length; j++, k++)
        {
            vec[j] = new Vector3d(-0.85, tickposy[k], (zmax / 2));
            t3[j] = new Transform3D();
            t3[j].setTranslation(vec[j]);
            t3[j].setScale(0.02);
            tg[j] = new TransformGroup();
            tg[j].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tg[j].setTransform(t3[j]);
            double temp = tickposy[k]*4;
            tg[j].addChild(new Shape3D(new Text3D(new Font3D(new Font("Book Antiqua", Font.BOLD, 1), new FontExtrusion()), df.format(temp), new Point3f()), textAppearance()));
        }
        
        for(int k = 0; k < tickposz.length; j++, k++)
        {
            vec[j] = new Vector3d(-0.88, 0, tickposz[k]);
            t3[j] = new Transform3D();
            t3[j].setTranslation(vec[j]);
            t3[j].setScale(0.02);
            tg[j] = new TransformGroup();
            tg[j].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            tg[j].setTransform(t3[j]);
            double temp = (tickposz[k] + (zmax / 2)) * 5000;
            tg[j].addChild(new Shape3D(new Text3D(new Font3D(new Font("Book Antiqua", Font.BOLD, 1), new FontExtrusion()), df.format(temp), new Point3f()), textAppearance()));
        }
        
        vec[j] = new Vector3d((xmax - 0.8) / 2, -0.05, (zmax / 2));
        t3[j] = new Transform3D();
        t3[j].setTranslation(vec[j]);
        t3[j].setScale(0.02);
        tg[j] = new TransformGroup();
        tg[j].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg[j].setTransform(t3[j]);
        tg[j].addChild(new Shape3D(new Text3D(new Font3D(new Font("Book Antiqua", Font.BOLD, 1), new FontExtrusion()), "NET", new Point3f()), textAppearance()));
        tg[j++].setPickable(false);
        
        vec[j] = new Vector3d(-0.95, 0.15, (zmax / 2));
        t3[j] = new Transform3D();
        t3[j].setTranslation(vec[j]);
        t3[j].setScale(0.02);
        tg[j] = new TransformGroup();
        tg[j].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg[j].setTransform(t3[j]);
        tg[j++].addChild(new Shape3D(new Text3D(new Font3D(new Font("Book Antiqua", Font.BOLD, 1), new FontExtrusion()), "Intensity", new Point3f()), textAppearance()));
        
        vec[j] = new Vector3d(-0.95, 0, (zmax / 4));
        t3[j] = new Transform3D();
        t3[j].setTranslation(vec[j]);
        t3[j].setScale(0.02);
        tg[j] = new TransformGroup();
        tg[j].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg[j].setTransform(t3[j]);
        tg[j].addChild(new Shape3D(new Text3D(new Font3D(new Font("Book Antiqua", Font.BOLD, 1), new FontExtrusion()), "Mass", new Point3f()), textAppearance()));
    }

    public Appearance barAppearance(int r, int g, int b, int type)
    {
        Appearance app = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(new Color(r, g, b)), ColoringAttributes.NICEST);
        app.setColoringAttributes(ca);

        PolygonAttributes pat = new PolygonAttributes();
        if(type == 0)
        {
            pat.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        }
        else
        {
            pat.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        }
        pat.setCullFace(PolygonAttributes.CULL_BACK);
        app.setPolygonAttributes(pat);

        LineAttributes la = new LineAttributes();
        la.setLinePattern(LineAttributes.PATTERN_SOLID);
        la.setLineWidth(4f);
        la.setLineWidth(0.5f);
        la.setLineAntialiasingEnable(true);
        app.setLineAttributes(la);

        return app;
    }

    public Appearance axisAppearance(int r, int g, int b, int type)
    {
        Appearance app = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(new Color(r, g, b)), ColoringAttributes.NICEST);
        app.setColoringAttributes(ca);

        PolygonAttributes pat = new PolygonAttributes();
        if(type == 0)
        {
            pat.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        }
        else
        {
            pat.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        }
        pat.setCullFace(PolygonAttributes.CULL_BACK);
        app.setPolygonAttributes(pat);

        LineAttributes la = new LineAttributes();
        la.setLinePattern(LineAttributes.PATTERN_SOLID);
        la.setLineWidth(4f);
        la.setLineAntialiasingEnable(true);
        app.setLineAttributes(la);

        TransparencyAttributes ta = new TransparencyAttributes();
        ta.setTransparencyMode(TransparencyAttributes.BLENDED);
        ta.setTransparency(0.5f);
        app.setTransparencyAttributes(ta);

        return app;
    }
    
    public Appearance textAppearance()
    {
        Appearance app = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(Color.CYAN), ColoringAttributes.NICEST);
        app.setColoringAttributes(ca);
        
        return app;
    }

    public final void createSceneGraph()
    {
        for(int i = 0; i < obj; i++)
        {
            scene.addChild(tg[i]);
        }
        scene.compile();
    }

    public final void extractCrd2D()
    {
        remove(canvas);
        panelcen.removeAll();

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series[] = new XYSeries[10];
        for(int i = 0; i < 10; i++)
        {
            series[i] = new XYSeries("Charge" + (i+1));
        }
        for(int i = 0; i < mass.length; i++)
        {
            series[charge[i]-1].add(net[i], mass[i]);
        }
        for(int i = 0; i < 10; i++)
        {
            dataset.addSeries(series[i]);
        }
        JFreeChart chart = ChartFactory.createScatterPlot("Mass vs NET", "NET", "Mass", dataset, PlotOrientation.VERTICAL, true, true, true);
        scatterpanel = new ChartPanel(chart, 800, 500, 800, 500, 800, 500, true, true, true, true, true, true)
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                ToolTipManager ttm = ToolTipManager.sharedInstance();
                ttm.setInitialDelay(0);
                ttm.setReshowDelay(0);
                ttm.setDismissDelay(0);
            }
            
            @Override
            public void mouseClicked(MouseEvent event)
            {
                String result = null;
                Insets insets = getInsets();
                int x = (int) ((event.getX() - insets.left) / this.getScaleX());
                int y = (int) ((event.getY() - insets.top) / this.getScaleY());

                this.setAnchor(new Point2D.Double(x, y));
                this.getChart().setNotify(true);

                ChartEntity entity = null;
                if(this.getChartRenderingInfo() != null)
                {
                    EntityCollection entities = this.getChartRenderingInfo().getEntityCollection();
                    if(entities != null)
                    {
                        entity = entities.getEntity(x, y);
                        if(entity != null)
                        {
                            result = entity.getToolTipText();
                            if(result != null)
                            {
                                panelInfo2D(result.substring(result.indexOf("(") + 1, result.indexOf(",")), result.substring(result.indexOf(",") + 1, result.indexOf(")")).replace(",", ""), MouseEvent.MOUSE_CLICKED);
                            }
                        }
                    }
                }
            }
            
            @Override
            public String getToolTipText(MouseEvent e)
            {
                String result = null;
                if(this.getChartRenderingInfo() != null)
                {
                    EntityCollection entities = this.getChartRenderingInfo().getEntityCollection();
                    if(entities != null)
                    {
                        Insets insets = getInsets();
                        ChartEntity entity = entities.getEntity((int) ((e.getX() - insets.left) / this.getScaleX()), (int) ((e.getY() - insets.top) / this.getScaleY()));
                        if(entity != null)
                        {
                            result = entity.getToolTipText();
                            if(result != null)
                            {
                                panelInfo2D(result.substring(result.indexOf("(") + 1, result.indexOf(",")), result.substring(result.indexOf(",") + 1, result.indexOf(")")), MouseEvent.MOUSE_ENTERED);
                            }
                        }
                    }
                }
                return result;
            }
        };
        scatterpanel.setMouseWheelEnabled(true);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 2;
        panelcen.add(scatterpanel, c);
        add(panelcen, BorderLayout.CENTER);
        panelcen.revalidate();
    }
    
    public void extractCrdHM() throws IOException
    {
        remove(canvas);
        remove(panelcen);
        panelcen.removeAll();
        
        double data[][] = new double[mass.length][2];        
        for(int i = 0; i < mass.length; i++)
        {
            data[i] = new double[] {1.0, 1.0};
        }
        CategoryDataset dataset = DatasetUtilities.createCategoryDataset("Series", "Category", data);

        JFreeChart chart = ChartFactory.createStackedBarChart("Control vs Cancer", null, "Glycopeptide", dataset, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        StackedBarRenderer renderer = new CustomRenderer(paint);
        CategoryAxis axis = new CustomCategoryAxis();
        renderer.setBarPainter(new StandardBarPainter());
        plot.setRenderer(renderer);
        plot.setDomainAxis(axis);

        
        scatterpanel = new ChartPanel(chart, 800, 500, 800, 500, 800, 500, true, true, true, true, true, true)
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                ToolTipManager ttm = ToolTipManager.sharedInstance();
                ttm.setInitialDelay(0);
                ttm.setReshowDelay(0);
            }
            
            @Override
            public void mouseClicked(MouseEvent event)
            {
                Insets insets = getInsets();
                int x = (int) ((event.getX() - insets.left) / this.getScaleX());
                int y = (int) ((event.getY() - insets.top) / this.getScaleY());

                this.setAnchor(new Point2D.Double(x, y));
                this.getChart().setNotify(true);

                ChartEntity entity = null;
                if(this.getChartRenderingInfo() != null)
                {
                    EntityCollection entities = this.getChartRenderingInfo().getEntityCollection();
                    Iterator iter = entities.iterator();
                    if(entities != null)
                    {
                        entity = entities.getEntity(x, y);
                        if(entity != null)
                        {
                            int count = 0;
                            while(iter.hasNext())
                            {
                                Object obj = iter.next();
                                if(obj != null)
                                {
                                    if(obj.getClass().getName().contains("CategoryItemEntity"))
                                    {
                                        CategoryItemEntity item = (CategoryItemEntity) obj;
                                        if(item.getShapeCoords().equals(entity.getShapeCoords()))
                                        {
                                            if(count > mass.length - 1)
                                            {
                                                count -= mass.length;
                                            }
                                            panelInfoHM(count, MouseEvent.MOUSE_CLICKED);
                                            break;
                                        }
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            @Override
            public String getToolTipText(MouseEvent e)
            {
                if(this.getChartRenderingInfo() != null)
                {
                    EntityCollection entities = this.getChartRenderingInfo().getEntityCollection();
                    Iterator iter = entities.iterator();
                    if(entities != null)
                    {
                        Insets insets = getInsets();
                        ChartEntity entity = entities.getEntity((int) ((e.getX() - insets.left) / this.getScaleX()), (int) ((e.getY() - insets.top) / this.getScaleY()));
                        if(entity != null)
                        {
                            int count = 0;
                            while(iter.hasNext())
                            {
                                Object obj = iter.next();
                                if(obj != null)
                                {
                                    if(obj.getClass().getName().contains("CategoryItemEntity"))
                                    {
                                        CategoryItemEntity item = (CategoryItemEntity) obj;
                                        if(item.getShapeCoords().equals(entity.getShapeCoords()))
                                        {
                                            if(count > mass.length - 1)
                                            {
                                                count -= mass.length;
                                            }
                                            panelInfoHM(count, MouseEvent.MOUSE_ENTERED);
                                            break;
                                        }
                                        count++;
                                    }
                                }
                            }
                        }
                    }
                }
                return null;
            }
        };
        scatterpanel.setMouseWheelEnabled(true);
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 2;
        panelcen.add(scatterpanel, c);
        add(panelcen, BorderLayout.CENTER);
        
        panelcen.revalidate();
    }
    
    public void panelInfoHM(int count, int type)
    {
        paneltopu.removeAll();
        Label m = null, n = null, lab = null;
        t1 = "Mass: " + df.format(mass[count]);
        m = new Label(t1);
        m.setFont(new Font("Book Antiqua", Font.BOLD, 12));
        m.setForeground(Color.RED);
        paneltopu.add(m);
        t2 = "NET: " + df.format(net[count]);
        n = new Label(t2);
        n.setFont(new Font("Book Antiqua", Font.BOLD, 12));
        n.setForeground(Color.RED);
        paneltopu.add(n);
        if(type == MouseEvent.MOUSE_CLICKED)
        {
            paneltopb.removeAll();
            String str = "Protein ID: " + proteinid[count];
            lab = new Label(str);
            lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
            lab.setForeground(Color.RED);
            paneltopb.add(lab);
            str = "Peptide: " + peptide[count];
            lab = new Label(str);
            lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
            lab.setForeground(Color.RED);
            paneltopb.add(lab);
            str = "Glycan: " + glycan[count];
            lab = new Label(str);
            lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
            lab.setForeground(Color.RED);
            paneltopb.add(lab);
            if(cidlen[count] > 0)
            {
                cidimg = createSpectra(cidlen[count], cidspec[count], "CID Spectra");
                sbcid = new SpecButton(cidimg);
            }
            else
            {
                sbcid = new SpecButton("CID Spectra");
            }
            if(hcdlen[count] > 0)
            {
                hcdimg = createSpectra(hcdlen[count], hcdspec[count], "HCD Spectra");
                sbhcd = new SpecButton(hcdimg);
            }
            else
            {
                sbhcd = new SpecButton("HCD Spectra");
            }
            panelsid.removeAll();
            panelsid.add(sbcid);
            panelsid.add(sbhcd);
        }
        else
        {
            paneltopu.removeAll();
            m = new Label(t1);
            m.setForeground(Color.CYAN);
            paneltopu.add(m);
            n = new Label(t2);
            n.setForeground(Color.CYAN);
            paneltopu.add(n);
        }
        paneltop.revalidate();
    }
    
    public void panelInfo2D(String x, String y, int type)
    {
        paneltopu.removeAll();
        Label m = null, n = null, lab = null;
        t1 = "Mass: " + y;
        m = new Label(t1);
        m.setFont(new Font("Book Antiqua", Font.BOLD, 12));
        m.setForeground(Color.RED);
        paneltopu.add(m);
        t2 = "NET: " + x;
        n = new Label(t2);
        n.setFont(new Font("Book Antiqua", Font.BOLD, 12));
        n.setForeground(Color.RED);
        paneltopu.add(n);
        if(type == MouseEvent.MOUSE_CLICKED)
        {
            for(int i = 0; i < mass.length; i++)
            {
                if((Math.round(mass[i] * 1000.0) / 1000.0) == Double.valueOf(y))
                {
                    paneltopb.removeAll();
                    String str = "Protein ID: " + proteinid[i];
                    lab = new Label(str);
                    lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                    lab.setForeground(Color.RED);
                    paneltopb.add(lab);
                    str = "Peptide: " + peptide[i];
                    lab = new Label(str);
                    lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                    lab.setForeground(Color.RED);
                    paneltopb.add(lab);
                    str = "Glycan: " + glycan[i];
                    lab = new Label(str);
                    lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                    lab.setForeground(Color.RED);
                    paneltopb.add(lab);
                    if(cidlen[i] > 0)
                    {
                        cidimg = createSpectra(cidlen[i], cidspec[i], "CID Spectra");
                        sbcid = new SpecButton(cidimg);
                    }
                    else
                    {
                        sbcid = new SpecButton("CID Spectra");
                    }
                    if(hcdlen[i] > 0)
                    {
                        hcdimg = createSpectra(hcdlen[i], hcdspec[i], "HCD Spectra");
                        sbhcd = new SpecButton(hcdimg);
                    }
                    else
                    {
                        sbhcd = new SpecButton("HCD Spectra");
                    }
                    panelsid.removeAll();
                    panelsid.add(sbcid);
                    panelsid.add(sbhcd);
                    break;
                }
            }
        }
        else
        {
            paneltopu.removeAll();
            m = new Label(t1);
            m.setForeground(Color.CYAN);
            paneltopu.add(m);
            n = new Label(t2);
            n.setForeground(Color.CYAN);
            paneltopu.add(n);
        }
        paneltop.revalidate();
    }
    
    public void panelInfo3D(MouseEvent me, int type)
    {
        pc.setShapeLocation(me);
        pc.setTolerance(0.0f);
        pr = pc.pickClosest();

        BranchGroup brgr;
        TransformGroup trgr;
        Transform3D trans = new Transform3D();
        Vector3d v3 = new Vector3d();
        Box boxo, boxc = null;
        Label m = null, n = null, lab = null;
        
        if(pr != null)
        {
            boxo = (Box)(Primitive)pr.getNode(PickResult.PRIMITIVE);
            brgr = pc.getBranchGroup();

            Enumeration num = brgr.getAllChildren();
            for(int i = 0; num.hasMoreElements(); i++)
            {
                trgr = (TransformGroup)num.nextElement();
                try
                {
                    if(!trgr.getChild(0).getClass().getName().equals("javax.media.j3d.Shape3D"))
                    {
                        boxc = (Box) trgr.getChild(0);
                    }
                }
                catch(ClassCastException e)
                {
                    e.printStackTrace();
                }
                if(boxo != null)
                {
                    if(boxo.hashCode() == boxc.hashCode())
                    {
                       trgr.getTransform(trans);
                       trans.get(v3);
                       break;
                    }
                }
            }
            
            if(v3.x != 0 && v3.y != 0 && v3.z != 0 && v3.z != -0.65)
            {
                paneltopu.removeAll();
                double temp1 = (v3.z + (zmax / 2)) * 5000;
                t1 = "Mass: " + df.format(temp1);
                m = new Label(t1);
                m.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                m.setForeground(Color.RED);
                paneltopu.add(m);
                double temp2 = (v3.x + 0.8) / 1.6;
                t2 = "NET: " + df.format(temp2);
                n = new Label(t2);
                n.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                n.setForeground(Color.RED);
                paneltopu.add(n);
                if(type == 1)
                {
                    for(int i = 0; i < mass.length; i++)
                    {
                        if(df.format(temp1).equals(df.format(mass[i])) && df.format(temp2).equals(df.format(net[i])))
                        {
                            paneltopb.removeAll();
                            String str = "Protein ID: " + proteinid[i];
                            lab = new Label(str);
                            lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                            lab.setForeground(Color.RED);
                            paneltopb.add(lab);
                            str = "Peptide: " + peptide[i];
                            lab = new Label(str);
                            lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                            lab.setForeground(Color.RED);
                            paneltopb.add(lab);
                            str = "Glycan: " + glycan[i];
                            lab = new Label(str);
                            lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                            lab.setForeground(Color.RED);
                            paneltopb.add(lab);
                            str = "Site: " + site[i];
                            lab = new Label(str);
                            lab.setFont(new Font("Book Antiqua", Font.BOLD, 12));
                            lab.setForeground(Color.RED);
                            paneltopb.add(lab);
                            if(cidlen[i] > 0)
                            {
                                cidimg = createSpectra(cidlen[i], cidspec[i], "CID Spectra");
                                sbcid = new SpecButton(cidimg);
                            }
                            else
                            {
                                sbcid = new SpecButton("CID Spectra");
                            }
                            if(hcdlen[i] > 0)
                            {
                                hcdimg = createSpectra(hcdlen[i], hcdspec[i], "HCD Spectra");
                                sbhcd = new SpecButton(hcdimg);
                            }
                            else
                            {
                                sbhcd = new SpecButton("HCD Spectra");
                            }
                            panelsid.removeAll();
                            panelsid.add(sbcid);
                            panelsid.add(sbhcd);
                            break;
                        }
                    }
                }
            }
            else
            {
                paneltopu.removeAll();
                m = new Label(t1);
                m.setForeground(Color.CYAN);
                paneltopu.add(m);
                n = new Label(t2);
                n.setForeground(Color.CYAN);
                paneltopu.add(n);
            }
            paneltop.revalidate();
        }
    }
    
    public ChartPanel createSpectra(int peaks, String spectra, String title)
    {
        XYSeries series = new XYSeries(title);
        byte[] data = Base64.decode(spectra);
        ByteBuffer bf = ByteBuffer.wrap(data);
        float[] mz = new float[peaks];
        float[] intensity = new float[peaks];

        for(int i = 0; i < peaks; i++)
        {
            mz[i] = bf.getFloat();
            intensity[i] = bf.getFloat();
            series.add(mz[i], intensity[i]);
        }
        
        IntervalXYDataset dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYBarChart(title, "M/Z", false, "Intensity", dataset, PlotOrientation.VERTICAL, true, true, false);
        imagepanel = new ChartPanel(chart);
        imagepanel.setMouseWheelEnabled(true);
        
        return imagepanel;
    }
    
    @Override
    public void mouseClicked(MouseEvent me)
    {
        panelInfo3D(me, 1);
    }

    @Override
    public void mouseMoved(MouseEvent me)
    {
        panelInfo3D(me, 0);
    }

    @Override
    public final void init()
    {
        new Runnable()
        {
            @Override
            public void run()
            {
                initComponents();
            }
        };
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">                                                    
    private void initComponents()
    {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        setLayout(new BorderLayout());
    }  
    // </editor-fold>                                                
    // Variables declaration - do not modify                     
    // End of variables declaration                                      

    public static void main(String[] args)
    {
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        new MainFrame(new GlycoMap(), screensize.width-15, screensize.height-75);
    }
  
    @Override
    public void mousePressed(MouseEvent me) {}

    @Override
    public void mouseReleased(MouseEvent me) {}

    @Override
    public void mouseEntered(MouseEvent me) {}

    @Override
    public void mouseExited(MouseEvent me) {}

    @Override
    public void mouseDragged(MouseEvent me) {}
    
    private Coordinates crd;
    private Canvas3D canvas;
    private Panel paneltop, paneltopu, paneltopb, panelbot, panelsid, panelcen;
    private ChartPanel scatterpanel, imagepanel, cidimg, hcdimg;
    private Button b2d, b3d, bhm;
    private SpecButton sbcid, sbhcd;
    private PickCanvas pc;
    private BranchGroup scene;
    private SimpleUniverse su;
    private BoundingSphere bs;
    private OrbitBehavior ob;
    private Transform3D t3[];
    private TransformGroup tg[];
    private Vector3d vec[];
    private PickResult pr;
    private Paint paint[];
    private DecimalFormat df;
    private Double mass[], net[], intca[], intco[], tickposx[], tickposy[], tickposz[];
    private String peptide[], glycan[], proteinid[], site[], cidspec[], hcdspec[], t1, t2;
    private double xmax, zmax, heightca, heightco;
    private int obj, cidlen[], hcdlen[], nzca[], nzco[], charge[];
}

class CustomRenderer extends StackedBarRenderer
{
    private Paint[] colors;
    
    public CustomRenderer(final Paint[] colors)
    {
        this.colors = colors;
    }
    
    @Override
    public Paint getItemPaint(final int row, final int column)
    {
        return this.colors[(row * 2) + column];
    }
}

class CustomCategoryAxis extends CategoryAxis
{
    @Override
    public java.util.List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, RectangleEdge edge)
    {
        java.util.List ticks = new java.util.ArrayList();

        if (dataArea.getHeight() <= 0.0 || dataArea.getWidth() < 0.0)
        {
            return ticks;
        }

        CategoryPlot plot = (CategoryPlot) getPlot();
        java.util.List categories = plot.getCategoriesForAxis(this);
        double max = 0.0;

        if (categories != null)
        {
            CategoryLabelPosition position = this.getCategoryLabelPositions().getLabelPosition(edge);
            float r = this.getMaximumCategoryLabelWidthRatio();
            if (r <= 0.0)
            {
                r = position.getWidthRatio();
            }

            float l = 0.0f;
            if (position.getWidthType() == CategoryLabelWidthType.CATEGORY)
            {
                l = (float) calculateCategorySize(categories.size(), dataArea, edge);
            }
            else
            {
                if (RectangleEdge.isLeftOrRight(edge))
                {
                    l = (float) dataArea.getWidth();
                }
                else
                {
                    l = (float) dataArea.getHeight();
                }
            }
            int categoryIndex = 0;
            Iterator iterator = categories.iterator();
            String str[] = new String[]{"Control", "Cancer"};
            while (iterator.hasNext())
            {
                Comparable category = (Comparable) iterator.next();
                g2.setFont(getTickLabelFont(category));
                TextBlock label = createLabel(str[categoryIndex%2], l * r, edge, g2);
                if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM)
                {
                    max = Math.max(max, calculateTextBlockHeight(label, position, g2));
                }
                else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT)
                {
                    max = Math.max(max, calculateTextBlockWidth(label, position, g2));
                }
                Tick tick = new CategoryTick(category, label, position.getLabelAnchor(), position.getRotationAnchor(), position.getAngle());
                ticks.add(tick);
                categoryIndex = categoryIndex + 1;
            }
        }
        state.setMax(max);
        return ticks;
    }
}

class SpecButton extends Button
{
    private ChartPanel img;
    private BufferedImage icon;
    
    public SpecButton(String text)
    {
        super.setLabel(text);
        setBackground(Color.BLACK);
        setForeground(Color.CYAN);
        
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                final Frame dialog = new Frame();
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                dialog.setPreferredSize(new Dimension(200, 100));
                dialog.add(new Label("Spectra Information Not Available"), BorderLayout.CENTER);
                int w = dialog.getSize().width;
                int h = dialog.getSize().height;
                int x = (dim.width - w)/3;
                int y = (dim.height - h)/3;
                dialog.setLocation(x, y);
                
                dialog.addWindowListener(new java.awt.event.WindowAdapter()
                {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e)
                    {
                        dialog.setVisible(false);
                    }
                });
                
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }
    
    public SpecButton(ChartPanel imagepanel)
    {
        img = imagepanel;
        icon = img.getChart().createBufferedImage(800, 500, img.getChartRenderingInfo());
        setBackground(Color.BLACK);
        
        addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                final Frame dialog = new Frame();
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                dialog.setPreferredSize(new Dimension(800, 500));
                dialog.add(img);
                int w = dialog.getSize().width;
                int h = dialog.getSize().height;
                int x = (dim.width - w)/8;
                int y = (dim.height - h)/8;
                dialog.setLocation(x, y);
                
                dialog.addWindowListener(new java.awt.event.WindowAdapter()
                {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e)
                    {
                        dialog.setVisible(false);
                    }
                });
                
                dialog.pack();
                dialog.setVisible(true);
            }
        });
    }
    
    @Override
    public void paint(Graphics g)
    {
        Graphics2D g2D = (Graphics2D) g;
        g2D.drawImage(icon, 10, 10, getWidth() - 20, getHeight() - 20, null);
    }
}