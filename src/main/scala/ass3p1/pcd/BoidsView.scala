package ass3p1.pcd

import ass3p1.MySystem.Command.{StartWorld, StopWorld}

import java.awt.*
import javax.swing.*
import javax.swing.event.{ChangeEvent, ChangeListener}

class BoidsView(model: BoidsModel, width: Int, height: Int) extends ChangeListener {

  private val frame: JFrame = new JFrame("Boids Simulation")
  frame.setSize(width, height)
  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)

  private val cp: JPanel = new JPanel()
  val layout: LayoutManager = new BorderLayout()
  cp.setLayout(layout)

  private val topPanel = new JPanel()
  cp.add(BorderLayout.NORTH, topPanel)
  topPanel.add(new JLabel("Boids Number"))
  val txt = new TextField(5)
  topPanel.add(txt)

  private val startButton = new JButton("Start")
  startButton.addActionListener(x =>
    println("Start button pressed")
    model.createBoids(txt.getText.toInt)
    model.actor ! StartWorld(txt.getText.toInt, model.getBoids)
    println(model.actor.printTree)
  )

  private val stopButon = new JButton("Stop")
  stopButon.addActionListener(x =>
    println("Stop button pressed")
    model.clearBoids()
    model.actor ! StopWorld
  )

  topPanel.add(startButton)
  topPanel.add(stopButon)

  private val boidsPanel: BoidsPanel = new BoidsPanel(this, model)
  cp.add(BorderLayout.CENTER, boidsPanel)

  private val slidersPanel: JPanel = new JPanel()

  private val cohesionSlider: JSlider = makeSlider()
  private val separationSlider: JSlider = makeSlider()
  private val alignmentSlider: JSlider = makeSlider()

  slidersPanel.add(new JLabel("Separation"))
  slidersPanel.add(separationSlider)
  slidersPanel.add(new JLabel("Alignment"))
  slidersPanel.add(alignmentSlider)
  slidersPanel.add(new JLabel("Cohesion"))
  slidersPanel.add(cohesionSlider)

  cp.add(BorderLayout.SOUTH, slidersPanel)

  frame.setContentPane(cp)

  frame.setVisible(true)

  private def makeSlider(): JSlider = {
    val slider = new JSlider(0, 0, 20, 10)
    slider.setMajorTickSpacing(10)
    slider.setMinorTickSpacing(1)
    slider.setPaintTicks(true)
    slider.setPaintLabels(true)
    //val labelTable: mutable.HashMap[Int, JLabel] = mutable.HashMap()
    val labelTable: java.util.Hashtable[Integer, JLabel] = new java.util.Hashtable()
    labelTable.put(0, new JLabel("0"))
    labelTable.put(10, new JLabel("1"))
    labelTable.put(20, new JLabel("2"))
    slider.setLabelTable(labelTable)
    slider.setPaintLabels(true)
    slider.addChangeListener(this)
    slider
  }

  def update(frameRate: Int): Unit = {
    boidsPanel.setFrameRate(frameRate)
    boidsPanel.repaint()
  }

  override def stateChanged(e: ChangeEvent): Unit = {
    if (e.getSource == separationSlider) {
      val a = separationSlider.getValue
      model.setSeparationWeight(0.1 * a)
    } else if (e.getSource == cohesionSlider) {
      val b = cohesionSlider.getValue
      model.setCohesionWeight(0.1 * b)
    } else {
      val cc = alignmentSlider.getValue
      model.setAlignmentWeight(0.1 * cc)
    }
  }

  def getWidth: Int = width

  def getHeight: Int = height

}
