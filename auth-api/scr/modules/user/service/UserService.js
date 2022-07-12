import bcrypt from "bcrypt";

import userRepository from "../repository/userRepository.js";
import * as httpStatus from "../../../config/constants/httpStatus.js"
import UserException from "../exception/UserException.js";

class UserService {

    async findByEmail(req) {
        try {
          const { email } = req.params;
          const { authUser } = req;
          this.validarDadosRequisicao(email);
          let user = await userRepository.findByEmail(email);
          this.validarUserNaoEncontrado(user);
          this.validateAuthenticatdUser(user, authUser);
          return {
            status: httpStatus.SUCCESS,
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
            },
          };
        } catch (err) {
            return {
                status: err.status ? err.status : httpStatus.INTERNAL_SERVER_ERROR,
                message: err.status,
            };
        }
    }

    validarDadosRequisicao(email) {
        if (!email) {
            throw new UserException(
                httpStatus.BAD_REQUEST,
                "User email was not informed"
            );
        }
    }

    validarUserNaoEncontrado(user) {
        if (!user) {
            throw new Error(
                httpStatus.BAD_REQUEST,
                "User was not found"
            );
        }
    }

    validateAuthenticatdUser(user, authUser) {
        if (!authUser || user,id !== authUser.id) {
            throw new UserException(httpStatus.FORBIDDEN, 'You cannot see this user id')
        }
    }

    async getAccessToken() {
        try {
            const {email, password} = req.body;
            this.validateAccessTokenData(email, password);
            let user = await userRepository.findByEmail(email);
            this.validateUserNotFound(user);
            this.validatePassword(password, user.password);
            const authUser = {id: user.id, name: user.name, email: user.email};
            const accessToken = jwt.sign({authUser}, secrets.API_SECRET, {
                experesIn: '1d',
            });
            return {
                status: httpStatus.SUCCESS,
                accessToken,
            };
        } catch (error) {
            return {
                status: err.status ? err.status : httpStatus.INTERNAL_SERVER_ERROR,
                message: err.status,
            };
        };
    }

    validateAccessTokenData(email, password) {
        if (!email || !password) {
          throw new UserException(
            httpStatus.UNAUTHORIZED,
            "Email and password must be informed."
          );
        }
      }

    validatePassword(password, hashPassword) {
        if (!( bcrypt.compare(password, hashPassword))) {
          throw new UserException(
            httpStatus.UNAUTHORIZED,
            "Password doesn't match."
          );
        }
    }
}

export default new UserService();