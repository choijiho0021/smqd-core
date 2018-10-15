package com.thing2x.smqd.net.telnet

import java.io.{EOFException, IOException}
import java.net.InetAddress

import com.typesafe.scalalogging.StrictLogging
import net.wimpi.telnetd.io.BasicTerminalIO
import net.wimpi.telnetd.io.toolkit.Editfield
import net.wimpi.telnetd.net.{Connection, ConnectionEvent}
import net.wimpi.telnetd.shell.Shell

// 10/15/18 - Created by Kwon, Yeong Eon

object LoginShell {

  trait Delegate {
    def login(shell: LoginShell, user: String, password: String): Boolean = true
    def allow(shell: LoginShell, remoteAddress: InetAddress): Boolean = {
      //val address = remoteAddress.getHostName
      //StringUtil.compareCaseWildExp(address, pattern) == 0
      true
    }
  }

  def setDelegate(delegate: LoginShell.Delegate): Unit = {
    this.delegate = Option(delegate)
  }

  private var delegate: Option[LoginShell.Delegate] = None

  def createShell: Shell = new LoginShell(delegate)
}

/**
  *
  */
class LoginShell(delegate: Option[LoginShell.Delegate]) extends Shell with StrictLogging {

  private var term: BasicTerminalIO = _
  private var connection: Connection = _

  override def run(conn: Connection): Unit = {
    try {
      val address = conn.getConnectionData.getInetAddress
      if (!(delegate.isDefined && delegate.get.allow(this, address))) {
        conn.close()
        return
      }

      connection = conn
      term = connection.getTerminalIO

      // dont forget to register listener
      connection.addConnectionListener(this)

      // clear the screen and start from zero
      term.eraseScreen()
      term.homeCursor()

      for (_ <- 0 until 3) {
        term.write("Login: ")
        var ef = new Editfield(term, "login", 50)
        ef.run()
        val username = ef.getValue

        term.write("\r\nPassword: ")
        ef = new Editfield(term, "passwd", 50)
        ef.setPasswordField(true)
        ef.run()
        val password = ef.getValue
        term.flush()

        if (delegate.isEmpty) {
          if ("admin" == username && "password" == password) {
            connection.setNextShell("bsh")
            return
          }
          else if ("dummy" == username && "dummy" == password) {
            connection.setNextShell("dummy")
            return
          }
        }
        else if (delegate.get.login(this, username, password)) {
          connection.setNextShell("bsh")
          return
        }

        term.write("\r\nLogin incorrect\r\n\r\n")
      }

      term.homeCursor()
      term.eraseScreen()
      term.write("Goodbye!.\r\n\r\n")
      term.flush()

      connection.close()
    } catch {
      case e: EOFException =>
        logger.info("Client send quit signal.")
      case ex: Exception =>
        logger.error("run()", ex)
    }
  }

  override def connectionIdle(connectionEvent: ConnectionEvent): Unit = try {
    term.write("CONNECTION_IDLE")
    term.flush()
  } catch {
    case e: IOException =>
      logger.error("connectionIdle()", e)
  }

  override def connectionTimedOut(connectionEvent: ConnectionEvent): Unit = try {
    term.write("CONNECTION_TIMEDOUT")
    term.flush()
    // close connection
    connection.close()
  } catch {
    case ex: Exception =>
      logger.error("connectionTimedOut()", ex)
  }


  override def connectionLogoutRequest(connectionEvent: ConnectionEvent): Unit = try {
    term.write("CONNECTION_LOGOUTREQUEST")
    term.flush()
  } catch {
    case ex: Exception =>
      logger.error("connectionLogoutRequest()", ex)
  }

  override def connectionSentBreak(connectionEvent: ConnectionEvent): Unit = try {
    term.write("CONNECTION_BREAK")
    term.flush()
  } catch {
    case ex: Exception =>
      logger.error("connectionSentBreak()", ex)
  }
}
